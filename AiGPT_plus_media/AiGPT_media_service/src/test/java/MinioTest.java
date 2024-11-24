import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;


import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description 测试MinIO
 */

public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.132:9000")
                    .credentials("adminminio", "adminminio")
                    .build();

   //上传文件（图片）
    @Test
    public  void upload() {
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".jpgS");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
//                    .object("test001.mp4")
                    .object("001/test.jpg")//添加子目录
                    .filename("D:\\web\\test\\test.jpg")
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }

    }

    @Test
    public void delete(){
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket("testbucket").object("001/test001.mp4").build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    @Test
    public void test_getFile() throws Exception {
        GetObjectArgs testbucket = GetObjectArgs.builder().bucket("testbucket").object("1.mp4").build();

        FilterInputStream inputStream = minioClient.getObject(testbucket);
        FileOutputStream outputStream = new FileOutputStream(new File("E:\\web\\Heima\\upload\\1_2.mp4"));
        IOUtils.copy(inputStream,outputStream);

        //校验文件的完整性对文件的内容进行md5
        String source_md5 = DigestUtils.md5Hex(inputStream);
        FileInputStream fileInputStream = new FileInputStream(new File("E:\\web\\Heima\\upload\\1_3.mp4"));
        String local_md5 = DigestUtils.md5Hex(fileInputStream);

        if(source_md5.equals(local_md5)){
            System.out.println("下载成功");
        }
    }


    //分块上传minio
    @Test
    public void uploadChunk() throws Exception {


        for (int i = 0; i < 9; i ++){
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("video")
                    .filename("D:\\web\\Heima\\bigfile_test\\chunk\\" + i)
                    .object("chunk/" + i)
                    .build();

            //上传
            minioClient.uploadObject(testbucket);
            System.out.println("上传分块" +i + "成功");
        }

    }

    //分块合并
    @Test
    public void testMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        List<ComposeSource> sources = new ArrayList<>();
//        for (int i = 0; i < 10;i++){
//            ComposeSource composeSource = ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build();
//            sources.add(composeSource);
//        }

        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(9).map(i -> ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build()).collect(Collectors.toList());

        //指定合并后的objectName等信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge02.mp4")
                .sources(sources)
                .build();

        minioClient.composeObject(composeObjectArgs);

    }


    //获取文件的md5
    @Test
    public void getMd5() {
        try (FileInputStream fileInputStream = new FileInputStream("D:\\web\\Heima\\bigfile_test\\c0540921e4dcd9238de4b26ed402e271.mp4")) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            System.out.println(fileMd5);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
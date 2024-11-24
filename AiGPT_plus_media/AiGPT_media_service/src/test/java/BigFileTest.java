import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @version 1.0
 * @description 大文件处理测试（分块处理）
 */
public class BigFileTest {

    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("D:\\web\\Heima\\bigfile_test\\test.mp4");
        //分块文件存储路径
        String chunkFilePath = "D:\\web\\Heima\\bigfile_test\\chunk\\";
        //分块文件大小
        int chunkSize = 1024 * 1024 * 5;
        System.out.println(sourceFile.length());
        //分块文件个数
        int chunkNum = (int)Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //使用流从文件读数据，向分块文件中写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile,"r");
        //缓冲区
        byte[] bytes = new byte[1024];
        for (int i = 0;i < chunkNum;i++){
            File chunkFile = new File(chunkFilePath + i);
            //分快文件写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile,"rw");

            int len = -1;
            while ((len = raf_r.read(bytes))!=-1){
                raf_rw.write(bytes,0,len);
                if(chunkFile.length() >= chunkSize){
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();

    }

    //将分块进行合并
    @Test
    public void testMerge() throws IOException {
        //块文件目录
        File chunkFolder = new File("E:\\web\\Heima\\bigfile_test\\chunk");
        //源文件
        File sourceFile = new File("E:\\web\\Heima\\bigfile_test\\test10086.mp4");
        //合并后
        File mergeFile = new File("E:\\web\\Heima\\bigfile_test\\test10086.mp4");

        //取出所以分块文件
        File[] files = chunkFolder.listFiles();
        //将数组转成LiSt
        List<File> fileList = Arrays.asList(files);

        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        //向合并文件写的流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile,"rw");
        //缓冲区
        byte[] bytes = new byte[1024];
        //遍历分块文件，后合并
        for (File file : fileList){
            RandomAccessFile raf_r = new RandomAccessFile(file,"r");
            int len = -1;
            while ((len = raf_r.read(bytes))!=-1){
                raf_rw.write(bytes,0,len);
            }
            raf_r.close();
        }
        raf_rw.close();

        //合并文件完成后对合并文件的检验
        FileInputStream fileInputStream_merge = new FileInputStream(mergeFile);
        FileInputStream fileInputStream_source = new FileInputStream(sourceFile);
        String md5_merge = DigestUtils.md5Hex(fileInputStream_merge);
        String md5_source = DigestUtils.md5Hex(fileInputStream_source);
        if(md5_merge.equals(md5_source)){
            System.out.println("文件合并完成");
        }
    }
}

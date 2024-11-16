//package netty;
//
//import com.kimo.config.NettyServer;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.http.DefaultHttpHeaders;
//import io.netty.handler.codec.http.HttpHeaders;
//import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
//import io.netty.handler.codec.http.websocketx.WebSocketVersion;
//import io.netty.handler.ssl.SslContext;
//import io.netty.handler.ssl.SslContextBuilder;
//import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
//import lombok.extern.slf4j.Slf4j;
//
//import java.net.URI;
//
//
//@Slf4j
//public class WebSocketTestClient {
//    private static final String SERVER_URL = "ws://localhost:8889/webSocket/chart";
//
//    public static void main(String[] args) throws Exception {
//        URI uri = new URI(SERVER_URL);
//        new WebSocketTestClient().connect(uri);
//    }
//
//    private void connect(URI uri) throws Exception {
//        String scheme = uri.getScheme();
//        String host = uri.getHost();
//        int port = uri.getPort();
//
//        boolean ssl = "wss".equalsIgnoreCase(scheme); // Use SSL if the URI scheme is wss (WebSocket Secure)
//        final SslContext sslCtx;
//        if (ssl) {
//            sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
//        } else {
//            sslCtx = null;
//        }
//
//        EventLoopGroup group = new NioEventLoopGroup();
//        try {
//            HttpHeaders headers = new DefaultHttpHeaders();
//            WebSocketClientHandler handler = new WebSocketClientHandler(
//                    WebSocketClientHandshakerFactory.newHandshaker(
//                            uri, WebSocketVersion.V13, null, false, headers));
//
//            Bootstrap b = new Bootstrap();
//            b.group(group)
//                    .channel(NioSocketChannel.class)
//                    .handler(new WebSocketClientInitializer(sslCtx, handler));
//
//            // Connect to the server
//            Channel ch = b.connect(host, port).sync().channel();
//            handler.handshakeFuture().sync();
//
//            // Send a message to the WebSocket server
//            handler.sendMessage("{\"uid\": \"1854934298709209090\"}");
//
//            // Wait until the connection is closed
//            ch.closeFuture().sync();
//        } finally {
//            group.shutdownGracefully();
//        }
//    }
//}

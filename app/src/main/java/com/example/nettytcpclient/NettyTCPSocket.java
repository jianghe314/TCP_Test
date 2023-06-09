package com.example.nettytcpclient;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;


/**
 * Created by CIDI zhengxuan on 2021/5/22
 * QQ:1309873105
 */
public class NettyTCPSocket {
    private static NettyTCPSocket nettyTCPSocket;
    private static EventLoopGroup group;
    private static Bootstrap bootstrap;
    private static TcpDataListener dataListener;
    private static Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == 11){
                if(dataListener != null){
                    dataListener.getDataListener((String)msg.obj);
                }
            }else if(msg.what == 22){
                if(dataListener != null){
                    dataListener.TcpStatusListener((String)msg.obj);
                }
            }
        }
    };

    private NettyTCPSocket() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
    }

    public static NettyTCPSocket connect(final String IP, final int port){
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                if(nettyTCPSocket == null){
                    try {
                        nettyTCPSocket = new NettyTCPSocket();
                        bootstrap.group(group)
                                .channel(NioSocketChannel.class)
                                .option(ChannelOption.TCP_NODELAY,true)
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,10*1000)
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                                        //ByteBuf delimiter = Unpooled.copiedBuffer("$$".getBytes());
                                        //socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(2048,delimiter));
                                        //socketChannel.pipeline().addLast(new StringDecoder());
                                        //socketChannel.pipeline().addLast(new ReadTimeoutHandler(10));
                                        socketChannel.pipeline().addLast(new NettyTCPHandler());
                                    }
                                });
                        ChannelFuture channelFuture = bootstrap.connect(IP,port).awaitUninterruptibly();
                        channelFuture.channel().closeFuture().sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        group.shutdownGracefully();
                    }
                }
            }
        });
        return nettyTCPSocket;
    }

    private static class NettyTCPHandler extends SimpleChannelInboundHandler {
        //超时处理
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            super.userEventTriggered(ctx, evt);
            Message msg = handler.obtainMessage(22,"连接超时");
            handler.sendMessage(msg);
            ctx.close();
            Log.e("TCP","----> 连接超时");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            Message msg = handler.obtainMessage(22,"连接不可用");
            handler.sendMessage(msg);
            ctx.close();
            Log.e("TCP","----> 连接不可用");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
//            ByteBuffer result = getByteBuffer();
//            ByteBuf buf = Unpooled.buffer(result.remaining());
//            buf.writeBytes(result);
            //ctx.writeAndFlush("发送".getBytes());
            Message msg = handler.obtainMessage(22,"连接可用");
            handler.sendMessage(msg);
            Log.e("TCP","----> 连接可用"+msg);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            int size = buf.readableBytes();
            byte[] bytes = new byte[size];
            buf.readBytes(bytes);
            String result = new String(bytes,"UTF-8");
            Message data = handler.obtainMessage(11,result);
            handler.sendMessage(data);
            Log.e("TCP","read  size---> " + size);
            Log.e("TCP","read ---> " + result);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.close();
            Message msg = handler.obtainMessage(22,"连接断开");
            handler.sendMessage(msg);
            Log.e("TCP","----> 连接断开");
        }
    }

    public static interface TcpDataListener{
        void getDataListener(String msg);
        void TcpStatusListener(String status);
    }

    public static void setTcpDataListener(TcpDataListener TcpDataListener){
        dataListener = TcpDataListener;
    }
}

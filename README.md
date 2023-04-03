## myrpc
rpc框架，基于Spring+zookeeper+netty
**来源于： https://github.com/luxiaoxun/NettyRpc.git**

##### 通信： netty
##### 注册中心： zookeeper
##### 容器： Spring

### 主要思路
    1. 定义服务端与消费端的数据传输对象。消费者请求对象： RpcRequest；服务端响应对象： RpcResponse。
    2. 定义自定义注解，@RpcService/@RpcReference，用于扫描服务端的rpc服务/需要生成的代理对象。
    3. SpringContext扫描@RpcService注解，将对应服务保存并注册在Zookeeper中，为消费端提供。扫描@RpcReference注解，为含有该注解的属性set生成的代理对象，用于消费端的调用。
       消费端需要生成代理对象，并通过Netty框架给服务端发送请求。
    4. 消费端代理对象(implements InvocationHandler #invoke(...))，发送请求后，会等待服务端响应，拿到服务端的数据后保存在RpcFuture中，
       RpcFuture实现JUC的Future接口，在响应数据还未拿到时，会在tryAcquire阻塞，直到所释放，并返回数据。
    5. 此时，整个rpc调用完毕。
#### 服务端
    Netty提供丰富多样的处理器，并且可以自定义处理器。服务端在服务注册后，需要将所有服务以及对应的bean保存，用于消费端调用时去调用本地服务。使用Map，key由一定规则生成，value则为bean对象。这里的key生成规则取决于你的注解属性，或者自定义，保证服务端/消费端按照规则能够获取到。
    服务注册，会按照一定的规则生成znode节点的data，定义Protocol对象，将数据保存主要是为了**客户端在初始化时获取根节点下所有的子节点，解析数据获取所有服务相关信息，从而发起连接**
    ```java
    public class Protocol {
        String host;
        int port;
        List<ServiceInfo> serviceList;
    }
    ```
    1. 添加心跳处理机制，IdleStateHandler；以及数据的编解码处理器。
    2. 根据指定的规则，将服务名，即: serviceName, 和对应的服务版本。生成Key，从Map从获取到bean，通过反射发起调用。将方法调用的结果writeAndFlush回消费端。
#### 客户端
    相比服务端，客户端的处理相对复杂一些。
    RpcClient对象创建，会获取所有子节点，经过校验等操作，解析到节点数据，然后解析到host，port，从而与服务端发起连接。
    服务启动时，通过Spring特性，读取注解后，通过反射给对应注解的字段set代理对象。从而实现调用。
    使用Jdk自带的动态代理对象，进行调用。




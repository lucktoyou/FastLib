package com.fastlib.net.core;

/**
 * Created by sgfb on 2019/12/6 0006
 * E-mail:602687446@qq.com
 * 返回码定义
 * 1xx  消息
 * 2xx  成功
 * 3xx  重定向
 * 4xx  客户端请求异常
 * 5xx  服务器异常
 */
public interface ResponseCodeDefinition{

    /**
     * 客户端应当继续发送请求。这个临时响应是用来通知客户端它的部分请求已经被服务器接收，且仍未被拒绝。客户端应当继续发送请求的剩余部分，或者如果请求已经完成，忽略这个响应。服务器必须在请求完成后向客户端发送一个最终响应
     */
    int CONTINUE=100;

    /**
     * 服务器已经理解了客户端的请求，并将通过Upgrade 消息头通知客户端采用不同的协议来完成这个请求。在发送完这个响应最后的空行后，服务器将会切换到在Upgrade 消息头中定义的那些协议
     */
    int SWITCHING_PROTOCOLS=101;

    /**
     * 由WebDAV（RFC 2518）扩展的状态码，代表处理将被继续执行。
     */
    int PROCESSING=102;

    /**
     * 请求成功
     */
    int OK=200;

    /**
     * 请求已经被实现，而且有一个新的资源已经依据请求的需要而建立，且其 URI 已经随Location 头信息返回
     */
    int CREATED=201;

    /**
     * 服务器已接受请求，但尚未处理。正如它可能被拒绝一样，最终该请求可能会也可能不会被执行。在异步操作的场合下，没有比发送这个状态码更方便的做法了
     */
    int ACCEPTED=202;

    /**
     * 服务器已成功处理了请求，但返回的实体头部元信息不是在原始服务器上有效的确定集合，而是来自本地或者第三方的拷贝。当前的信息可能是原始版本的子集或者超集
     */
    int NON_AUTHORITATIVE_INFORMATION=203;

    /**
     * 服务器成功处理了请求，但不需要返回任何实体内容，并且希望返回更新了的元信息。响应可能通过实体头部的形式，返回新的或更新后的元信息。如果存在这些头部信息，则应当与所请求的变量相呼应
     */
    int NO_CONTENT=204;

    /**
     * 服务器成功处理了请求，且没有返回任何内容。但是与204响应不同，返回此状态码的响应要求请求者重置文档视图。该响应主要是被用于接受用户输入后，立即重置表单，以便用户能够轻松地开始另一次输入
     */
    int RESET_CONTENT=205;

    /**
     * 服务器已经成功处理了部分 GET 请求。类似于 FlashGet 或者迅雷这类的 HTTP下载工具都是使用此类响应实现断点续传或者将一个大文档分解为多个下载段同时下载
     */
    int PARTIAL_CONTENT=206;

    /**
     * 由WebDAV(RFC 2518)扩展的状态码，代表之后的消息体将是一个XML消息，并且可能依照之前子请求数量的不同，包含一系列独立的响应代码
     */
    int MULTI_STATUS=207;

    /**
     * 被请求的资源有一系列可供选择的回馈信息，每个都有自己特定的地址和浏览器驱动的商议信息。用户或浏览器能够自行选择一个首选的地址进行重定向
     */
    int MULTIPLE_CHOICES=300;

    /**
     * 被请求的资源已永久移动到新位置
     */
    int MOVED_PERMANENTLY=301;

    /**
     * 请求的资源临时从不同的URI响应请求
     */
    int MOVE_TEMPORARILY=302;

    /**
     * 对应当前请求的响应可以在另一个URL上被找到
     */
    int SEE_OTHER=303;

    /**
     * 如果客户端发送了一个带条件的 GET 请求且该请求已被允许，而文档的内容（自上次访问以来或者根据请求的条件）并没有改变，则服务器应当返回这个状态码
     */
    int NOT_MODIFIED=304;

    /**
     * 被请求的资源必须通过指定的代理才能被访问
     */
    int USE_PROXY=305;

    /**
     * 在最新版的规范中，306状态码已经不再被使用
     */
    @Deprecated
    int SWITCH_PROXY=306;

    /**
     * 请求的资源临时从不同的URI响应请求
     */
    int TEMPORARY_REDIRECT=307;

    /**
     * 1、语义有误，当前请求无法被服务器理解。除非进行修改，否则客户端不应该重复提交这个请求
     * 2、请求参数有误
     */
    int BAD_REQUEST=400;

    /**
     * 当前请求需要用户验证
     */
    int UNAUTHORIZED=401;

    /**
     * 预留
     */
    int PAYMENT_REQUIRED=402;

    /**
     * 服务器已经理解请求，但是拒绝执行它
     */
    int FORBIDDEN=403;

    /**
     * 请求失败，请求所希望得到的资源未被在服务器上发现
     */
    int NOT_FOUND=404;

    /**
     * 请求行中指定的请求方法不能被用于请求相应的资源。该响应必须返回一个Allow 头信息用以表示出当前资源能够接受的请求方法的列表
     */
    int METHOD_NOT_ALLOWED=405;

    /**
     * 请求的资源的内容特性无法满足请求头中的条件，因而无法生成响应实体
     */
    int NOT_ACCEPTABLE=406;

    /**
     * 与401响应类似，只不过客户端必须在代理服务器上进行身份验证
     */
    int PROXY_AUTHENTICATION_REQUIRED=407;

    /**
     * 请求超时.超过了服务器设置的等待时间
     */
    int REQUEST_TIMEOUT=408;

    /**
     * 由于和被请求的资源的当前状态之间存在冲突，请求无法完成
     */
    int CONFLICT=409;

    /**
     * 被请求的资源在服务器上已经不再可用，而且没有任何已知的转发地址
     */
    int GONE=410;

    /**
     * 服务器拒绝在没有定义 Content-Length 头的情况下接受请求
     */
    int LENGTH_REQUIRED=411;

    /**
     * 服务器在验证在请求的头字段中给出先决条件时，没能满足其中的一个或多个
     */
    int PROCONDITION_FAILED=412;

    /**
     * 服务器拒绝处理当前请求，因为该请求提交的实体数据大小超过了服务器愿意或者能够处理的范围。此种情况下，服务器可以关闭连接以免客户端继续发送此请求
     */
    int REQUEST_ENTITY_TOO_LARGE=413;

    /**
     * 请求的URI 长度超过了服务器能够解释的长度，因此服务器拒绝对该请求提供服务
     */
    int REQUEST_URI_TOO_LONG=414;

    /**
     * 对于当前请求的方法和所请求的资源，请求中提交的实体并不是服务器中所支持的格式
     */
    int UNSUPPORTED_MEDIA_TYPE=415;

    /**
     * 如果请求中包含了 Range 请求头，并且 Range 中指定的任何数据范围都与当前资源的可用范围不重合，同时请求中又没有定义 If-Range 请求头
     */
    int REQUETED_RANGE_NOT_SATISFIABLE=416;

    /**
     * 在请求头Expect中指定的预期内容无法被服务器满足
     */
    int EXPECTATION_FAILED=417;

    /**
     * 一个愚人节玩笑.可以在触发反爬虫机制时使用
     */
    int I_M_A_TEAPOT=418;

    /**
     * 从当前客户端所在的IP地址到服务器的连接数超过了服务器许可的最大范围
     */
    int TOO_MANY_CONNECTIONS=421;

    /**
     * 请求格式正确，但是由于含有语义错误，无法响应
     */
    int UNPROCESSABLE_ENTITY=422;

    /**
     * 当前资源被锁定
     */
    int LOCKED=423;

    /**
     * 由于之前的某个请求发生的错误，导致当前请求失败
     */
    int FAILED_DEPENDENCY=424;

    /**
     * 状态码 425 Too Early 代表服务器不愿意冒风险来处理该请求，原因是处理该请求可能会被“重放”，从而造成潜在的攻击
     */
    int TOO_EARLY=425;

    /**
     * 客户端应当切换到TLS/1.0
     */
    int UPGRADE_REQUIRED=426;

    /**
     * 由微软扩展，代表请求应当在执行完适当的操作后进行重试
     */
    int RETRY_WITH=449;

    /**
     * 该请求因法律原因不可用
     */
    int UNAVAILABLE_FOR_LEGAL_REASONS=451;

    /**
     * 服务器遇到了一个未曾预料的状况，导致了它无法完成对请求的处理
     */
    int INTERNAL_SERVER_ERROR=500;

    /**
     * 服务器不支持当前请求所需要的某个功能
     */
    int NOT_IMPLEMENTED=501;

    /**
     * 作为网关或者代理工作的服务器尝试执行请求时
     */
    int BAD_GATEWAY=502;

    /**
     * 由于临时的服务器维护或者过载，服务器当前无法处理请求
     */
    int SERVICE_UNAVAILABLE=503;

    /**
     * 作为网关或者代理工作的服务器尝试执行请求时，未能及时从上游服务器或者辅助服务器（例如DNS）收到响应
     */
    int GATEWAY_TIMEOUT=504;

    /**
     * 服务器不支持，或者拒绝支持在请求中使用的 HTTP 版本
     */
    int HTTP_VERSION_NOT_SUPPORTED=505;

    /**
     * 由《透明内容协商协议》（RFC 2295）扩展，代表服务器存在内部配置错误
     */
    int VARIANT_ALSO_NEGOTIATES=506;

    /**
     * 服务器无法存储完成请求所必须的内容
     */
    int INSUFFICIENT_STORAGE=507;

    /**
     * 服务器达到带宽限制。这不是一个官方的状态码，但是仍被广泛使用
     */
    int BANDWIDTH_LIMIT_EXCEEDED=509;

    /**
     * 获取资源所需要的策略并没有被满足
     */
    int NOT_EXTENDED=510;

    /**
     * 源站没有返回响应头部，只返回实体内容
     */
    int UNPARSEABLE_RESPONSE_HEADERS=600;
}
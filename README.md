# whitewall
基于SpringBoot的高校表白墙社交系统

- [快速开始](#快速开始)

搭建步骤

- [Schedule 1 -搭建环境](#Schedule-1)

- [Schedule 2 -基本框架开发/IOC和AOP/数据库配置和首页的创建/用户注册登录以及使用token](#Schedule-2)

- [Schedule 3 -新增发表问题功能，并防止xss注入以及敏感词过滤](#Schedule-3)

- [Schedule 4 -新增评论和站内信功能](#Schedule-4)

- [Schedule 5 -新增点赞和点踩功能，使用Redis实现](#Schedule-5)

- [Schedule 6 -新增异步消息功能 新增邮件发送组件](#Schedule-6)

- [Schedule 7 -新增关注功能，开发关注页面和粉丝页面](#Schedule-7)

- [Schedule 8 -Timeline与新鲜事，推拉模式下的Feed流](#Schedule-8)

- [Schedule 9 -python爬虫v2ex数据](#Schedule-9)

- [Schedule 10 -solr搭建全文搜索引擎](#Schedule-10)

- [Schedule 11 -单元测试与压力测试，项目部署等工作](#Schedule-11)

- [Schedule 12 -产品功能扩展以及技术深度扩展](#Schedule-12)
---
## 快速开始

根据`application.properties`文件中的mysql配置，在localhost:3306端口下启动mysql8，并且创建一个数据库whitewall，
接着去执行文件`whitewall/src/test/resources/init-schema.sql`，接着根据`whitewall/src/main/java/com.Dagon.whitewall/util/JedisAdapter`文件中配置的redis连接池，在localhost:6379启动redis服务。
接着就可以初步启动项目了。

启动成功如下：

![index](src/main/resources/static/images/res/index.png)

## Schedule 1
    创建git仓库，本地配置idea并测试pull和push。
    
    创建SpringBoot工程，导入Web，Velocity和Aop的包。
    
    生成Maven项目，pom.xml包含上述依赖，应用名称是whitewall,小组id是com.Dagon。

## Schedule 2
### 基本框架开发
    创建基本的controller，service和model层。

    controller中使用注解配置，requestmapping，responsebody基本可以解决请求转发以及响应内容的渲染。responsebody自动选择viewresolver进行解析。

    使用pathvariable和requestparam传递参数。

    使用velocity编写页面模板，注意其中的语法使用。常用$!{}和${}

    使用http规范下的httpservletrequest和httpservletresponse来封装请求和相响应，使用封装好的session和cookie对象。

    使用重定向的redirectview和统一异常处理器exceptionhandler
### IOC和AOP
    IOC解决对象实例化以及依赖传递问题，解耦。

    AOP解决纵向切面问题，主要实现日志和权限控制功能。

    aspect实现切面，并且使用logger来记录日志，用该切面的切面方法来监听controller。
### 数据库配置和首页的创建

    使用mysql创建数据库和表。
    
    加入mybatis和mysql的maven仓库，注意，由于现在版本的springboot不再支持velocity进而导致我使用较早版本的springboot，所以这里提供一可以正常运行的版本设置。

    springboot使用1.3.6

    mybatis-spring-boot-starter使用1.1.1

    mysql-connector-java使用8.0.19

    亲测可用。
    
    接下来写controller，dao和service。注意mybatis的注解语法以及xml的配置要求，xml要求放在resource中并且与dao接口在相同的包路径下。
    
    application.properties增加spring配置数据库链接地址
    
    两个小工具：
    ViewObject:方便传递任何数据到
    VelocityDateTool:velocity自带工具类
    
    写好静态文件html css和js。并且注意需要配置
    spring.velocity.suffix=.html 保证跳转请求转发到html上
    spring.velocity.toolbox-config-location=toolbox.xml
    
    至此主页基本完成，具体业务逻辑请参考代码。
### 用户注册登录以及使用token
    完成用户注册和登录的controller,service和dao层代码

    新建数据表login_ticket用来存储ticket字段。该字段在用户登录成功时被生成并存入数据库，并被设置为cookie，
    下次用户登录时会带上这个ticket，ticket是随机的uuid，有过期时间以及有效状态。

    使用拦截器interceptor来拦截所有用户请求，判断请求中是否有有有效的ticket，如果有的话则将用户信息写入Threadlocal。
    所有线程的threadlocal都被存在一个叫做hostholder的实例中，根据该实例就可以在全局任意位置获取用户的信息。

    该ticket的功能类似session，也是通过cookie写回浏览器，浏览器请求时再通过cookie传递，区别是该字段是存在数据库中的，并且可以用于移动端。

    通过用户访问权限拦截器来拦截用户的越界访问，比如用户没有管理员权限就不能访问管理员页面。

    配置了用户的webconfiguration来设置启动时的配置，这里可以将上述的两个拦截器加到启动项里。

    配置了json工具类以及md5工具类，并且使用Java自带的salt生成api将用户密码加密为密文。保证密码安全。

    数据安全性的保障手段：https使用公钥加密私钥解密，比如支付宝的密码加密，单点登录验证，验证码机制等。

    ajax异步加载数据 json数据传输等。
## Schedule 3
### 新增发表问题功能，并防止xss注入以及敏感词过滤
    新增Question相关的model，dao，service和controller。顺带写了一下Message和Comment相关的model，dao。

    发布问题时检查标题和内容，防止xss注入，并且过滤敏感词。

    防止xss注入直接使用HTMLutils的方法即可实现。

    过滤敏感词首先需要建立一个字典树，并且读取一份保存敏感词的文本文件，然后初始化字典树。
    最后将过滤器作为一个服务，让需要过滤敏感词的服务进行调用即可。
## Schedule 4
### 新增评论和站内信功能
    首先建立表comment和message分别代表评论中心和站内信。
    
    依次开发model，dao，service和controller。
    
    评论的逻辑是每一个问题下面都有评论，显示评论数量，具体内容，评论人等信息。
    
    消息的逻辑是，两个用户之间发送一条消息，有一个唯一的会话id，这个会话里可以有多条这两个用户的交互信息。
    通过一个用户id获取该用户的会话列表，再根据会话id再获取具体的会话内的多条消息。
    
    逻辑清楚之后，再加上一些附加功能，比如显示未读消息数量（已读后就不显示），根据时间顺序排列会话和消息。
    
    本节内容基本就是业务逻辑的开发，没有新增什么技术点，主要是前后端交互的逻辑比较复杂，前端的开发量也比较大。
## Schedule 5
### 新增点赞和点踩功能，使用Redis实现
    首先了解一下redis的基础知识，数据结构，jedis使用等。

    编写list，string，hashm，set，sortset的测试用例，熟悉jedis api。

    开发点踩和点赞功能，在此之前根据业务封装好jedis的增删改查操作，放在util包中

    根据需求确定key字段，格式是 like，entityType，entityId 和 dislike，entityType，entityId

    将喜欢一条新闻的人存在一个集合，不喜欢的存在另一个集合。通过统计数量可以获得点赞和点踩数。

    一般点赞（点踩）操作是先增加likeKey（disLikeKey），然后删除disLikeKey（likeKey），最后返回likeKey集合的数量
## Schedule 6
### 新增异步消息功能 新增邮件发送组件
    在之前的功能中有一些不需要实时执行的操作或者任务，我们可以把它们改造成异步消息来进行发送。

    具体操作就是使用redis来实现异步消息队列。代码中我们使用事件Event来包装一个事件，事件需要记录事件实体的各种信息。

    我们在async包里开发异步工具类，事件生产者，事件消费者，并且开发一个EventHandler接口，让各种事件的实现类来实现这个接口。

    事件生产者一般作为一个服务，由业务代码进行调用产生一个事件。而事件消费者我们在代码里使用了单线程循环获取队列里的事件，并且寻找对应的xxHandler进行处理。

    如此一来，整个异步事件的框架就开发完成了。后面新加入的登录，点赞等事件都可以这么实现。

    新增邮件功能，主要是引入mail依赖，并且配置好自己的邮箱信息，以及邮件模板，同时在业务代码中加入发邮件的逻辑即可。

    PS：邮件采用了学校的企业邮箱，大家在做的时候可以试一试学校的企业邮箱。
## Schedule 7
### 新增关注功能，开发关注页面和粉丝页面
    新增关注功能，使用redis实现每一个关注对象的粉丝列表以及每一个用户的关注对象列表。

    通过该列表的crud操作可以对应获取粉丝列表和关注列表，并且实现关注和取关功能。

    由于关注成功和添加粉丝成功时同一个事务里的两个操作，可以使用redis的事务multi来开启事务，exec提交事务。

    除此之外，关注成功或者被关注还可以通过事件机制来生成发送邮件的事件（FollowHandler），由异步的队列处理器来完成事件响应，同样是根据redis来实现。

    对于粉丝列表，除了显示粉丝的基本信息之外，还要显示当前用户是否关注了这个粉丝，以便前端显示。

    对于关注列表来说，如果被关注对象是用户的话，除了显示用户的基本信息之外，还要显示当前用户是被这个用户关注，以便前端显示。

    PS：整合前后端进行调试时，遇到了点小bug，代码传参顺序传反了，导致粉丝列表时有时无，代码逻辑完全正确。

    查了redis数据库才发现数据有问题，意识到参数传递有问题，debug了将近一小时多，写代码还是要细心。。
## Schedule 8
### Timeline与新鲜事，推拉模式下的Feed流
    微博的新鲜事功能介绍：关注好友的动态，比如关注好友的点赞，发表的问题，关注了某个问题等信息，都是feed流的一部分。
    
    在知乎中的feed流主要体现于：关注用户的评论行为，关注用户的关注问题行为。
    
    feed流主要分为两种，推模式和拉模式，推模式主要是把新鲜事推送给关注该用户的粉丝，本例使用redis来存储某个用户接受的新鲜事id列表。
    
    这个信息流又称为timeline，根据用户的唯一key来存储。
    
    拉模式主要是用户直接找寻自己所有关注的人，并且到数据库去查找这些关注对象的新鲜事，直接返回。
    
    推模式主要适合粉丝较少的小用户，因为他们的粉丝量少，使用推模式产生的冗余副本也比较少，并且可以减少用户访问的压力。
    
    拉模式主要适合大v，因为很多僵尸粉根本不需要推送信息，用推模式发给这些僵尸粉就是浪费资源，所以让用户通过拉模式请求，只需要一个数据副本即可。
    
    同时如果是热点信息，这些信息也可以放在缓存，让用户首先拉取这些信息，提高查询效率。
    
    使用feedhandler异步处理上述的两个事件，当事件发生时，根据事件实体进行重新包装，构造一个新鲜事，因为所有新鲜事的格式是一样的。
    
    需要包括：日期，新鲜事类型，发起者，新鲜事内容，然后把该数据存入数据库，以便用户使用pull模式拉出。
    
    为了适配推送模式，此时也要把新鲜事放到该用户所有粉丝的timeline里，这样的话我们就同时实现了推和拉的操作了。
## Schedule 9
### python爬虫v2ex数据
    安装python2.7并且配置环境变量。同时安装pycharm，配置interpretor，安装pip。
    
    安装好以后，熟悉一下python的基本语法，写一些例子，比如数据类型，操作符，方法调用，以及面向对象的技术。
    
    因为数据是要导入数据库的，所以这里安装MySQLdb的一个库，并且写一下连接数据库的代码，写一下简单的crud进行测试。
    
    使用requests库作为解析http请求的工具，使用beautifulsoup作为解析html代码的工具，请求之后直接使用css选择器匹配。即可获得内容。
    
    当然现在我们有更方便的工具pyspider，可以方便解析请求并且可以设置代理，伪装身份等，直接传入url并且写好多级的解析函数，程序便会迭代执行，直到把所有页面的内容解析出来。
    
    这里我们直接启动pyspider的web应用并且写好python代码，就可以执行爬虫了。简单讲一下知乎和v2ex的爬虫流程。
    
    v2ex：
    首先请求首页，因为v2ex现在也是https页面了，所以需要默认把使用证书设为false。
    
    执行完index_page函数，说明首页已经请求完毕，我们了解其css布局和url特征以后，根据tab=?可以进入下一级分类。于是for循环爬出所有以tab=?结尾的url，并且分别请求，进入下一级函数。
    
    根据页面的层级和css格式我们设置好多级函数依次循环执行，这样我们就可以解析到最后一级真正的帖子内容了。
    
    当然最后一级内容需要调用数据库的存储接口，为了避免存储错误，需要把内容中的 " 改成 //，否则会出问题。

## Schedule 10
### solr搭建全文搜索引擎
    solr是一个成熟的全文搜索引擎工具，底层是Lucene实现，主要是java语言写的
    
    下载solr6.2。完成solr环境搭建，简单测试多副本部署和单机部署。
    
    solr默认英文分词，需要加入中文分词工具IK-Analyzer
    
    solr中一个core代表一个全文搜索集，我们可以在server文件夹中找到我们创建的
    core。然后根据需要修改conf里的配置文件，首先修改managed-schema来设置分词规则，我们在此加入中文分词类型，并且配置其索引分词和查询分词，此处需要引入IK-Analyzer的jar包，jar包可以通过maven项目打包而获得。
    
    索引分词指的是建立索引使用的分词，比如你好上海，可以分为你 你好 上海 上 等情况。
    而查询分词是根据需求进行查询时的分词，可以分为你好 上海。
    
    为了通过数据库向solr导入数据，我们需要配置数据导入处理器，这是需要修改solrconfig文件来配置数据导入处理器，并且在solr-data-config中配置本地数据库地址，这样就可以在solr的web页面中进行数据库导入了。导入之后自动建立索引，我们就可以使用solr来对数据库进行全文搜索了。比如mysql数据库进行普通搜索，把数据导入solr进行全文搜索。
    
    开发搜索相关功能，开发service并且接入solr的api，从而连接本机的solr服务并且执行查询和索引操作。
    只需要指定关键字，以及我们要搜索的栏目（本例中主要title和content，所以传入这两个字段，并且在搜索结果中加亮关键字。
    开发相关controller以及页面。并且在新增问题以后执行异步事件，将新增的问题数据加入solr的数据库中，以便后面进行搜索。
    
## Schedule 11
### 单元测试与压力测试，项目部署等工作
    单元测试保证模块的可用性，每个模块测试完以后再进行集成测试，maven打包时会自动执行单元测试。
    SpringBoot中只需在test类中做好配置便可以进行spring相关的单元测试。
    
    使用压测工具apache2-utils， LoadRunner等进行压力测试，更好地了解系统性能
    
    centos上支持Apachebench压测工具，可以并发发送大量http请求来完成压力测试，可以看出机器的负载状况。
    
    在虚拟机上安装jdk8,tomcat8,redis,maven,nginx,mysql等基本环境。
    tomcat默认监听127.0.0.1的8080端口，只允许本地访问，这样保障安全。
    所以可以在外网访问时加入一层Nginx进行反向代理和负载均衡，让域名或ip访问首先找到Nginx，再由nginx找到tomcat。
    
    solr服务仅内网访问：./solr start -Djetty.host=127.0.0.1
    
    Nginx 配置 /etc/nginx/sites-enabled/c3
    server {
    	listen 80;
    	server_name example.dagon.com;
    	location / {
    		proxy_pass http://127.0.0.1:8080;
    	}
    }
    
## Schedule 12
### 产品功能扩展以及技术深度扩展
    产品功能扩展
    
    1. 用户注册，邮箱激活流程
    2. 首页滚动到底部自动加载更多
    3. 管理员后台管理
    4. 运营推荐问题置顶
    5. timeline推拉结合
    6. 个性化首页，timeline更多事件
    
    技术深度扩展
    
    1. 搜索结果排序打分
    2. 爬虫覆盖用户，评论，内容去html标签
    3. 个性化推荐
---
## 参考文档
- [redis](https://redis.io/)
- [spring](https://spring.io/)

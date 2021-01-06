# 作业说明
## Week12 作业题目：
1.（必做）配置 redis 的主从复制，sentinel 高可用，Cluster 集群。

**实验环境说明：**

操作系统：win10 家庭版

Redis：Redis-x64-3.2.100

（1）配置redis的主从复制

（a）配置一主二从，master的配置文件采用原始自带的redis.windows.conf，下面是该文件去除掉注释的部分，原始配置文件放在master-slave-conf目录下

```
bind 127.0.0.1
protected-mode yes
port 6379
tcp-backlog 511
timeout 0
tcp-keepalive 0
loglevel notice
logfile ""
databases 16
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
dbfilename dump.rdb
dir ./
slave-serve-stale-data yes
slave-read-only yes
repl-diskless-sync no
repl-diskless-sync-delay 5
repl-disable-tcp-nodelay no
slave-priority 100
appendonly no
appendfilename "appendonly.aof"
appendfsync everysec
no-appendfsync-on-rewrite no

auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
aof-load-truncated yes
lua-time-limit 5000
slowlog-log-slower-than 10000
slowlog-max-len 128
latency-monitor-threshold 0
notify-keyspace-events ""
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-size -2
list-compress-depth 0
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
hll-sparse-max-bytes 3000

activerehashing yes
client-output-buffer-limit normal 0 0 0
client-output-buffer-limit slave 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60
hz 10
aof-rewrite-incremental-fsync yes
```

配置两个从的Redis服务器配置，一个使用6380端口，一个使用6381端口，都是6379端口对应的Redis的slave。两个配置文件如下：

```
port 6380
bind 127.0.0.1
slaveof 127.0.0.1 6379
```

```
port 6381
bind 127.0.0.1
slaveof 127.0.0.1 6379
```

（b）启动，在redis目录下依次用以下终端启动以下命令：

```
redis-server.exe redis.windows.conf
redis-server.exe redis.windows6380.conf
redis-server.exe redis.windows6381.conf
```

在端口6379的master在控制台输出

```
[7872] 05 Jan 11:22:26.391 # Server started, Redis version 3.2.100
[7872] 05 Jan 11:22:26.391 * DB loaded from disk: 0.000 seconds
[7872] 05 Jan 11:22:26.392 * The server is now ready to accept connections on port 6379
```

在端口6380的slave的终端

```
[14412] 05 Jan 15:01:45.200 # Server started, Redis version 3.2.100
[14412] 05 Jan 15:01:45.201 * DB loaded from disk: 0.000 seconds
[14412] 05 Jan 15:01:45.201 * The server is now ready to accept connections on port 6380
[14412] 05 Jan 15:01:45.201 * Connecting to MASTER 127.0.0.1:6379
[14412] 05 Jan 15:01:45.201 * MASTER <-> SLAVE sync started
[14412] 05 Jan 15:01:45.201 * Non blocking connect for SYNC fired the event.
[14412] 05 Jan 15:01:45.201 * Master replied to PING, replication can continue...
[14412] 05 Jan 15:01:45.202 * Partial resynchronization not possible (no cached master)
[14412] 05 Jan 15:01:45.212 * Full resync from master: 69025c3359f298c8d518e279a12be24f80b4fba2:1
[14412] 05 Jan 15:01:45.671 * MASTER <-> SLAVE sync: receiving 123 bytes from master
[14412] 05 Jan 15:01:45.675 * MASTER <-> SLAVE sync: Flushing old data
[14412] 05 Jan 15:01:45.675 * MASTER <-> SLAVE sync: Loading DB in memory
[14412] 05 Jan 15:01:45.677 * MASTER <-> SLAVE sync: Finished with success
```

在端口6381的slave的终端

```
[3916] 05 Jan 15:04:50.459 # Server started, Redis version 3.2.100
[3916] 05 Jan 15:04:50.460 * DB loaded from disk: 0.000 seconds
[3916] 05 Jan 15:04:50.460 * The server is now ready to accept connections on port 6381
[3916] 05 Jan 15:04:50.460 * Connecting to MASTER 127.0.0.1:6379
[3916] 05 Jan 15:04:50.460 * MASTER <-> SLAVE sync started
[3916] 05 Jan 15:04:50.460 * Non blocking connect for SYNC fired the event.
[3916] 05 Jan 15:04:50.460 * Master replied to PING, replication can continue...
[3916] 05 Jan 15:04:50.460 * Partial resynchronization not possible (no cached master)
[3916] 05 Jan 15:04:50.470 * Full resync from master: 69025c3359f298c8d518e279a12be24f80b4fba2:253
[3916] 05 Jan 15:04:50.933 * MASTER <-> SLAVE sync: receiving 123 bytes from master
[3916] 05 Jan 15:04:50.937 * MASTER <-> SLAVE sync: Flushing old data
[3916] 05 Jan 15:04:50.937 * MASTER <-> SLAVE sync: Loading DB in memory
[3916] 05 Jan 15:04:50.938 * MASTER <-> SLAVE sync: Finished with success
```

可以看到6380和6381已经是6379的从了。

此时看6379的启动服务终端显示：

```
[7872] 05 Jan 11:22:26.391 # Server started, Redis version 3.2.100
[7872] 05 Jan 11:22:26.391 * DB loaded from disk: 0.000 seconds
[7872] 05 Jan 11:22:26.392 * The server is now ready to accept connections on port 6379
[7872] 05 Jan 15:01:45.202 * Slave 127.0.0.1:6380 asks for synchronization
[7872] 05 Jan 15:01:45.202 * Full resync requested by slave 127.0.0.1:6380
[7872] 05 Jan 15:01:45.202 * Starting BGSAVE for SYNC with target: disk
[7872] 05 Jan 15:01:45.212 * Background saving started by pid 14568
[7872] 05 Jan 15:01:45.658 # fork operation complete
[7872] 05 Jan 15:01:45.662 * Background saving terminated with success
[7872] 05 Jan 15:01:45.672 * Synchronization with slave 127.0.0.1:6380 succeeded
[7872] 05 Jan 15:04:50.460 * Slave 127.0.0.1:6381 asks for synchronization
[7872] 05 Jan 15:04:50.461 * Full resync requested by slave 127.0.0.1:6381
[7872] 05 Jan 15:04:50.461 * Starting BGSAVE for SYNC with target: disk
[7872] 05 Jan 15:04:50.470 * Background saving started by pid 20916
[7872] 05 Jan 15:04:50.927 # fork operation complete
[7872] 05 Jan 15:04:50.930 * Background saving terminated with success
[7872] 05 Jan 15:04:50.934 * Synchronization with slave 127.0.0.1:6381 succeeded
```

可以看到两个从（6380和6381）已经请求主（6379）进行同步了，并且同步成功。

（c）进行主从操作

此时打开一个终端，用“redis-cli -h 127.0.0.1 -p 6379”连接到master的redis服务

```
127.0.0.1:6379> keys *
1) "count"
2) "inventory:skuid"
127.0.0.1:6379> set mykey 1
OK
127.0.0.1:6379>
```

可以看见原先有两值，再重新，添加一个新的键值。在打开一个终端，用“redis-cli -h 127.0.0.1 -p 6379”连接到6380的slave的redis服务

```
127.0.0.1:6380> keys *
1) "inventory:skuid"
2) "count"
3) "mykey"
127.0.0.1:6380> get mykey
"1"
```

可以看到slave已经同步了master服务器。执行“info replication”，显示

```
127.0.0.1:6380> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6379
master_link_status:up
master_last_io_seconds_ago:6
master_sync_in_progress:0
slave_repl_offset:2197
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

再6380的客户端进行新的键值添加操作，是不成功的，显示如下：

```
127.0.0.1:6380> set mykey2 2
(error) READONLY You can't write against a read only slave.
```

用终端连接到6381，重复显示同样信息，这里不再赘述。

（d）主从切换

将6379的master的服务停掉。在6380的redis的客户端，执行“slaveof no one”

```
127.0.0.1:6380> slaveof no one
OK
127.0.0.1:6380> set mykey 2
OK
127.0.0.1:6380>
```

可以看到，现在6380不再是从了，而且可以给现有的键重新赋值。

在6381的客户端中设置为6380的从，执行“slaveof 127.0.0.1 6380”

```
127.0.0.1:6381> slaveof 127.0.0.1 6380
OK
```

执行“info replication”

```
127.0.0.1:6381> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6380
master_link_status:up
master_last_io_seconds_ago:2
master_sync_in_progress:0
slave_repl_offset:29
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

已经切换了6380为master，6381为slave了。

```
127.0.0.1:6381> get mykey
"2"
```

也可以获取刚才6380设置的新的值了。

启动6379，并且作为6380的slave，执行：

```
redis-server --slaveof 127.0.0.1 6380
```

启动后，控制台输出

```
[2024] 05 Jan 15:51:49.639 # Server started, Redis version 3.2.100
[2024] 05 Jan 15:51:49.639 * DB loaded from disk: 0.000 seconds
[2024] 05 Jan 15:51:49.639 * The server is now ready to accept connections on port 6379
[2024] 05 Jan 15:51:49.640 * Connecting to MASTER 127.0.0.1:6380
[2024] 05 Jan 15:51:49.640 * MASTER <-> SLAVE sync started
[2024] 05 Jan 15:51:49.640 * Non blocking connect for SYNC fired the event.
[2024] 05 Jan 15:51:49.640 * Master replied to PING, replication can continue...
[2024] 05 Jan 15:51:49.640 * Partial resynchronization not possible (no cached master)
[2024] 05 Jan 15:51:49.651 * Full resync from master: 90f36dfb7b497c94f8281e4b10f02fac59cd52b0:1107
[2024] 05 Jan 15:51:49.828 * MASTER <-> SLAVE sync: receiving 132 bytes from master
[2024] 05 Jan 15:51:49.831 * MASTER <-> SLAVE sync: Flushing old data
[2024] 05 Jan 15:51:49.831 * MASTER <-> SLAVE sync: Loading DB in memory
[2024] 05 Jan 15:51:49.831 * MASTER <-> SLAVE sync: Finished with success
```

已经是6380的slave了。使用“redis-cli -h 127.0.0.1 -p 6379”进入6379的客户端，执行“info replication”

```
127.0.0.1:6379> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6380
master_link_status:up
master_last_io_seconds_ago:5
master_sync_in_progress:0
slave_repl_offset:1401
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

```
127.0.0.1:6379> keys *
1) "inventory:skuid"
2) "count"
3) "mykey"
127.0.0.1:6379> get mykey
"2"
```

可以看到数据也同步了。

主从方式的配置文件放在master-slave-conf目录下

（2）sentinel 高可用

（a）先配置一主二从，配置文件和配置启动方式同上，这里不再赘述。

（b）配置sentinel，写三个sentinel的配置文件，sentinel26379.conf、sentinel26380.conf、sentinel26381.conf。分别如下：

sentinel26379.conf

```
port 26379
sentinel myid 20c656411e0b231f93aa9f12ce6e118fd39b165f
sentinel monitor mymaster 127.0.0.1 6380 2
sentinel down-after-milliseconds mymaster 60000
sentinel failover-timeout mymaster 180000
sentinel parallel-syncs mymaster 1
```

sentinel26380.conf

```
port 26380
sentinel myid 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317
sentinel monitor mymaster 127.0.0.1 6380 2
sentinel down-after-milliseconds mymaster 60000
sentinel failover-timeout mymaster 180000
sentinel parallel-syncs mymaster 1
```

sentinel26381.conf

```
port 26381
sentinel myid 6f16c41cf571eb4a3ad20650d13dc1874cf18cd4
sentinel monitor mymaster 127.0.0.1 6380 2
sentinel down-after-milliseconds mymaster 60000
sentinel failover-timeout mymaster 180000
sentinel parallel-syncs mymaster 1
```

注：这三个命令行中myid可以不写，现在这个是执行之后自动生成，同时也会生产其他一些配置，这个部分后面会提到。

（c）启动

启动一主二从，方式和前面的一样，这里不再赘述。

启动setinel，启动三个控制台分别运行下面的命令

```
redis-server.exe sentinel26379.conf --sentinel
redis-server.exe sentinel26380.conf --sentinel
redis-server.exe sentinel26381.conf --sentinel
```

因为，之前执行过程中已经将6380切换成master，而6379和6381是slave。现在进入redis客户端进行验证执行

```
redis-cli -h 127.0.0.1 -p 6380
```

进入客户端后，执行info replication，显示如下：

```
127.0.0.1:6380> info replication
# Replication
role:master
connected_slaves:2
slave0:ip=127.0.0.1,port=6379,state=online,offset=40109,lag=1
slave1:ip=127.0.0.1,port=6381,state=online,offset=40242,lag=0
master_repl_offset:40389
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:2
repl_backlog_histlen:40388
```

开启另一个控制台，执行redis-cli -h 127.0.0.1 -p 6379，进入redis客户端后，执行info replication，显示如下：

```
127.0.0.1:6379> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6380
master_link_status:up
master_last_io_seconds_ago:1
master_sync_in_progress:0
slave_repl_offset:29266
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

然后另一个控制台，执行redis-cli -h 127.0.0.1 -p 6381，进入redis客户端后，执行info replication，显示如下：

```
127.0.0.1:6381> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6380
master_link_status:up
master_last_io_seconds_ago:1
master_sync_in_progress:0
slave_repl_offset:125047
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

可以看出确实一主二从，master是6380，slave是6379和6381。

（d）切换主从

现在将master6380下线，在客户端执行shutdown。

等60秒，sentinel判断master下线，重新选择新的master，这次选的是6381。可以从sentinel的控制输出看见。

26379的sentinel的控制台显示如下：

```
[4624] 05 Jan 22:34:24.254 # +sdown master mymaster 127.0.0.1 6380
[4624] 05 Jan 22:34:24.344 # +new-epoch 1
[4624] 05 Jan 22:34:24.348 # +vote-for-leader 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317 1
[4624] 05 Jan 22:34:25.389 # +odown master mymaster 127.0.0.1 6380 #quorum 3/2
[4624] 05 Jan 22:34:25.389 # Next failover delay: I will not start a failover before Tue Jan 05 22:40:24 2021
[4624] 05 Jan 22:34:25.464 # +config-update-from sentinel 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317 127.0.0.1 26380 @ mymaster 127.0.0.1 6380
[4624] 05 Jan 22:34:25.465 # +switch-master mymaster 127.0.0.1 6380 127.0.0.1 6381
[4624] 05 Jan 22:34:25.468 * +slave slave 127.0.0.1:6379 127.0.0.1 6379 @ mymaster 127.0.0.1 6381
[4624] 05 Jan 22:34:25.469 * +slave slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6381
[4624] 05 Jan 22:35:25.536 # +sdown slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6381
```

26380的sentinel的控制台显示如下：

```
[2504] 05 Jan 22:34:24.273 # +sdown master mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:24.336 # +odown master mymaster 127.0.0.1 6380 #quorum 2/2
[2504] 05 Jan 22:34:24.336 # +new-epoch 1
[2504] 05 Jan 22:34:24.336 # +try-failover master mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:24.340 # +vote-for-leader 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317 1
[2504] 05 Jan 22:34:24.349 # 6f16c41cf571eb4a3ad20650d13dc1874cf18cd4 voted for 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317 1
[2504] 05 Jan 22:34:24.349 # 20c656411e0b231f93aa9f12ce6e118fd39b165f voted for 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317 1
[2504] 05 Jan 22:34:24.431 # +elected-leader master mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:24.431 # +failover-state-select-slave master mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:24.489 # +selected-slave slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:24.489 * +failover-state-send-slaveof-noone slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:24.549 * +failover-state-wait-promotion slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:25.385 # +promoted-slave slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:25.386 # +failover-state-reconf-slaves master mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:25.463 * +slave-reconf-sent slave 127.0.0.1:6379 127.0.0.1 6379 @ mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:26.448 * +slave-reconf-inprog slave 127.0.0.1:6379 127.0.0.1 6379 @ mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:26.449 * +slave-reconf-done slave 127.0.0.1:6379 127.0.0.1 6379 @ mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:26.520 # -odown master mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:26.520 # +failover-end master mymaster 127.0.0.1 6380
[2504] 05 Jan 22:34:26.520 # +switch-master mymaster 127.0.0.1 6380 127.0.0.1 6381
[2504] 05 Jan 22:34:26.521 * +slave slave 127.0.0.1:6379 127.0.0.1 6379 @ mymaster 127.0.0.1 6381
[2504] 05 Jan 22:34:26.521 * +slave slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6381
[2504] 05 Jan 22:35:26.571 # +sdown slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6381
```

26381的sentinel的控制台显示如下：

```
[22968] 05 Jan 22:34:24.307 # +sdown master mymaster 127.0.0.1 6380
[22968] 05 Jan 22:34:24.344 # +new-epoch 1
[22968] 05 Jan 22:34:24.348 # +vote-for-leader 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317 1
[22968] 05 Jan 22:34:24.370 # +odown master mymaster 127.0.0.1 6380 #quorum 3/2
[22968] 05 Jan 22:34:24.370 # Next failover delay: I will not start a failover before Tue Jan 05 22:40:24 2021
[22968] 05 Jan 22:34:25.464 # +config-update-from sentinel 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317 127.0.0.1 26380 @ mymaster 127.0.0.1 6380
[22968] 05 Jan 22:34:25.467 # +switch-master mymaster 127.0.0.1 6380 127.0.0.1 6381
[22968] 05 Jan 22:34:25.469 * +slave slave 127.0.0.1:6379 127.0.0.1 6379 @ mymaster 127.0.0.1 6381
[22968] 05 Jan 22:34:25.470 * +slave slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6381
[22968] 05 Jan 22:35:25.519 # +sdown slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6381
```

可以看到发现下线以及判断线和投票选举新的master过程。

在6381的客户端执行info replication

```
127.0.0.1:6381> info replication
# Replication
role:master
connected_slaves:1
slave0:ip=127.0.0.1,port=6379,state=online,offset=16775,lag=0
master_repl_offset:16908
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:2
repl_backlog_histlen:16907
```

已经是master了。

在6379的客户端执行info replication

```
127.0.0.1:6379> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6381
master_link_status:up
master_last_io_seconds_ago:0
master_sync_in_progress:0
slave_repl_offset:20261
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

已经是6381的slave了。

这时，重新启动6380。在控制台执行redis-server.exe redis.windows6380.conf启动redis服务

在新的控制台执行redis-cli -h 127.0.0.1 -p 6380，进入6380的客户端，执行info replication

```
127.0.0.1:6380> info replication
# Replication
role:slave
master_host:127.0.0.1
master_port:6381
master_link_status:up
master_last_io_seconds_ago:0
master_sync_in_progress:0
slave_repl_offset:154635
slave_priority:100
slave_read_only:1
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

可以看到6380已经是slave了。

现在看一下三个sentinel的配置文件

sentinel26379.conf

```
port 26379
sentinel myid 20c656411e0b231f93aa9f12ce6e118fd39b165f
sentinel monitor mymaster 127.0.0.1 6381 2
sentinel down-after-milliseconds mymaster 60000
sentinel config-epoch mymaster 1
sentinel leader-epoch mymaster 1
# Generated by CONFIG REWRITE
dir "F:\\software\\Redis-x64-3.2.100"
sentinel known-slave mymaster 127.0.0.1 6380
sentinel known-slave mymaster 127.0.0.1 6379
sentinel known-sentinel mymaster 127.0.0.1 26381 6f16c41cf571eb4a3ad20650d13dc1874cf18cd4
sentinel known-sentinel mymaster 127.0.0.1 26380 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317
sentinel current-epoch 1
```

sentinel26380.conf

```
port 26380
sentinel myid 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317
sentinel monitor mymaster 127.0.0.1 6381 2
sentinel down-after-milliseconds mymaster 60000
sentinel config-epoch mymaster 1
sentinel leader-epoch mymaster 1
# Generated by CONFIG REWRITE
dir "F:\\software\\Redis-x64-3.2.100"
sentinel known-slave mymaster 127.0.0.1 6379
sentinel known-slave mymaster 127.0.0.1 6380
sentinel known-sentinel mymaster 127.0.0.1 26379 20c656411e0b231f93aa9f12ce6e118fd39b165f
sentinel known-sentinel mymaster 127.0.0.1 26381 6f16c41cf571eb4a3ad20650d13dc1874cf18cd4
sentinel current-epoch 1
```

sentinel26381.conf

```
port 26381
sentinel myid 6f16c41cf571eb4a3ad20650d13dc1874cf18cd4
sentinel monitor mymaster 127.0.0.1 6381 2
sentinel down-after-milliseconds mymaster 60000
sentinel config-epoch mymaster 1
sentinel leader-epoch mymaster 1
# Generated by CONFIG REWRITE
dir "F:\\software\\Redis-x64-3.2.100"
sentinel known-slave mymaster 127.0.0.1 6380
sentinel known-slave mymaster 127.0.0.1 6379
sentinel known-sentinel mymaster 127.0.0.1 26380 04a6bd1cb0f28cab2a0388e41adb587b8dd0c317
sentinel known-sentinel mymaster 127.0.0.1 26379 20c656411e0b231f93aa9f12ce6e118fd39b165f
sentinel current-epoch 1
```

注：sentinel的三个配置文件放在sentinel目录下，另外，运行后被sentinel自动修改后的配置文件放在/sentinel/运行后/的目录下。

（3）Cluster 集群

（a）通过Ruby脚本搭建Cluster集群，先搭建Ruby环境。因为使用的是Win10环境，需要在https://rubyinstaller.org/downloads/ 上下载rubyinstaller-2.7.2-1-x64.exe。然后根据https://rubygems.org/gems/redis/versions/3.2.0， 安装ruby的redis驱动

```
gem install redis -v 3.2.0
```

下载Redis官方提供的创建Redis集群的ruby脚本文件redis-trib.rb，路径如下：

https://raw.githubusercontent.com/MSOpenTech/redis/3.0/src/redis-trib.rb

将该脚本放置到redis安装目录下。

（b）配置集群

这次配置集群，计划配置三主三从的集群方式。

因此需要配置6个Redis服务。写6个配置文件在Redis目录下，分别是redis.windows-service-6380.conf、redis.windows-service-6381.conf、redis.windows-service-6382.conf、redis.windows-service-6383.conf、redis.windows-service-6384.conf、redis.windows-service-6385.conf，具体内容如下：

redis.windows-service-6380.conf

```
port 6380
loglevel notice
logfile "F:/software/Redis-x64-3.2.100/Logs/redis6380_log.txt"
appendonly yes
appendfilename "appendonly.6380.aof"
cluster-enabled yes
cluster-config-file nodes.6380.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.windows-service-6381.conf

```
port 6381
loglevel notice    
logfile "F:/software/Redis-x64-3.2.100/Logs/redis6381_log.txt"
appendonly yes
appendfilename "appendonly.6381.aof"
cluster-enabled yes
cluster-config-file nodes.6381.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.windows-service-6382.conf

```
port 6382
loglevel notice
logfile "F:/software/Redis-x64-3.2.100/Logs/redis6382_log.txt"
appendonly yes
appendfilename "appendonly.6382.aof"
cluster-enabled yes
cluster-config-file nodes.6382.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.windows-service-6383.conf

```
port 6383
loglevel notice    
logfile "F:/software/Redis-x64-3.2.100/Logs/redis6383_log.txt"
appendonly yes
appendfilename "appendonly.6383.aof"
cluster-enabled yes
cluster-config-file nodes.6383.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.windows-service-6384.conf

```
port 6384
loglevel notice
logfile "F:/software/Redis-x64-3.2.100/Logs/redis6384_log.txt"
appendonly yes
appendfilename "appendonly.6384.aof"
cluster-enabled yes
cluster-config-file nodes.6384.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

redis.windows-service-6385.conf

```
port 6385
loglevel notice
logfile "F:/software/Redis-x64-3.2.100/Logs/redis6385_log.txt"
appendonly yes
appendfilename "appendonly.6385.aof"
cluster-enabled yes
cluster-config-file nodes.6385.conf
cluster-node-timeout 15000
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

（c）搭建

先开启六个控制台终端，分别执行下面的命令，启动六个redis服务。

```
redis-server.exe redis.windows-service-6380.conf
redis-server.exe redis.windows-service-6381.conf
redis-server.exe redis.windows-service-6382.conf
redis-server.exe redis.windows-service-6383.conf
redis-server.exe redis.windows-service-6384.conf
redis-server.exe redis.windows-service-6385.conf
```

执行redis-trib.rb，搭建集群，开启一个控制台终端，执行下面的命令

```
redis-trib.rb create --replicas 1 127.0.0.1:6380 127.0.0.1:6381 127.0.0.1:6382 127.0.0.1:6383 127.0.0.1:6384 127.0.0.1:6385
```

执行完毕后，控制台输出

```
F:\software\Redis-x64-3.2.100>redis-trib.rb create --replicas 1 127.0.0.1:6380 127.0.0.1:6381 127.0.0.1:6382 127.0.0.1:6383 127.0.0.1:6384 127.0.0.1:6385
>>> Creating cluster
Connecting to node 127.0.0.1:6380: OK
Connecting to node 127.0.0.1:6381: OK
Connecting to node 127.0.0.1:6382: OK
Connecting to node 127.0.0.1:6383: OK
Connecting to node 127.0.0.1:6384: OK
Connecting to node 127.0.0.1:6385: OK
>>> Performing hash slots allocation on 6 nodes...
Using 3 masters:
127.0.0.1:6380
127.0.0.1:6381
127.0.0.1:6382
Adding replica 127.0.0.1:6383 to 127.0.0.1:6380
Adding replica 127.0.0.1:6384 to 127.0.0.1:6381
Adding replica 127.0.0.1:6385 to 127.0.0.1:6382
M: 2e7d21393bfbfb6f0555a74840452e1925d72128 127.0.0.1:6380
   slots:0-5460 (5461 slots) master
M: 04e87f0a9c7dd3e77979e5e93988d3b8838bf8db 127.0.0.1:6381
   slots:5461-10922 (5462 slots) master
M: c906b59f8e13cf52fc22b8843590f661aea39531 127.0.0.1:6382
   slots:10923-16383 (5461 slots) master
S: 6513356e49986489af2ccd37d9cb369f38107f54 127.0.0.1:6383
   replicates 2e7d21393bfbfb6f0555a74840452e1925d72128
S: f10666d4a43966139c6d74efeeb0fe82dbdc4423 127.0.0.1:6384
   replicates 04e87f0a9c7dd3e77979e5e93988d3b8838bf8db
S: 940085ce76e0aff7a47a0939bff4ecbdb5017833 127.0.0.1:6385
   replicates c906b59f8e13cf52fc22b8843590f661aea39531
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join....
>>> Performing Cluster Check (using node 127.0.0.1:6380)
M: 2e7d21393bfbfb6f0555a74840452e1925d72128 127.0.0.1:6380
   slots:0-5460 (5461 slots) master
M: 04e87f0a9c7dd3e77979e5e93988d3b8838bf8db 127.0.0.1:6381
   slots:5461-10922 (5462 slots) master
M: c906b59f8e13cf52fc22b8843590f661aea39531 127.0.0.1:6382
   slots:10923-16383 (5461 slots) master
M: 6513356e49986489af2ccd37d9cb369f38107f54 127.0.0.1:6383
   slots: (0 slots) master
   replicates 2e7d21393bfbfb6f0555a74840452e1925d72128
M: f10666d4a43966139c6d74efeeb0fe82dbdc4423 127.0.0.1:6384
   slots: (0 slots) master
   replicates 04e87f0a9c7dd3e77979e5e93988d3b8838bf8db
M: 940085ce76e0aff7a47a0939bff4ecbdb5017833 127.0.0.1:6385
   slots: (0 slots) master
   replicates c906b59f8e13cf52fc22b8843590f661aea39531
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
```

此时，集群已搭建完毕。

（d）访问操作

开启一个控制台终端，执行

```
redis-cli -c -h 127.0.0.1 -p 6380
```

注意一定要加“-c”，否则会在后面访问过程报错。

可以尝试获取key，添加一个key，如下：

```
F:\software\Redis-x64-3.2.100>redis-cli -c -h 127.0.0.1 -p 6380
127.0.0.1:6380> keys *
(empty list or set)
127.0.0.1:6380> set key1 1
-> Redirected to slot [9189] located at 127.0.0.1:6381
OK
127.0.0.1:6381> get key1
"1"
```

可以看到新加入的key1，已经放到6381（当前连接的是6380）。注意，此时客户端连接已经重定向到了6381，获取key1，可以获取到。

如果重新连接到6380，再获取key1，会先重定向到6381，再次输入“get key1”，才能获取到。如下：

```
F:\software\Redis-x64-3.2.100>redis-cli -c -h 127.0.0.1 -p 6380
127.0.0.1:6380> get key1
-> Redirected to slot [9189] located at 127.0.0.1:6381
"1"
127.0.0.1:6381> get key1
"1"
```

执行CLUSTER INFO，显示如下:

```
127.0.0.1:6381> CLUSTER INFO
cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:6
cluster_size:3
cluster_current_epoch:6
cluster_my_epoch:2
cluster_stats_messages_sent:2017
cluster_stats_messages_received:2017
```

可以看到6个节点，集群的size是3。

再执行cluster nodes，显示如下:

```
127.0.0.1:6381> cluster nodes
2e7d21393bfbfb6f0555a74840452e1925d72128 127.0.0.1:6380 master - 0 1609902203205 1 connected 0-5460
f10666d4a43966139c6d74efeeb0fe82dbdc4423 127.0.0.1:6384 slave 04e87f0a9c7dd3e77979e5e93988d3b8838bf8db 0 1609902202198 5 connected
04e87f0a9c7dd3e77979e5e93988d3b8838bf8db 127.0.0.1:6381 myself,master - 0 0 2 connected 5461-10922
940085ce76e0aff7a47a0939bff4ecbdb5017833 127.0.0.1:6385 slave c906b59f8e13cf52fc22b8843590f661aea39531 0 1609902198167 6 connected
6513356e49986489af2ccd37d9cb369f38107f54 127.0.0.1:6383 slave 2e7d21393bfbfb6f0555a74840452e1925d72128 0 1609902199175 4 connected
c906b59f8e13cf52fc22b8843590f661aea39531 127.0.0.1:6382 master - 0 1609902201191 3 connected 10923-16383
```

可以看到，6380是master，槽位0-5460，slave是6383。注意6383的slave的id和6380是一致的。

6381是master，槽位是5461-10922，slave是6384。

6382是master，槽位是10923-16383，slave是6385。

在6380的客户端上执行info replication，显示：

```
127.0.0.1:6380> info replication
# Replication
role:master
connected_slaves:1
slave0:ip=127.0.0.1,port=6383,state=online,offset=2493,lag=0
master_repl_offset:2493
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:2
repl_backlog_histlen:2492
```

在6381的客户端上执行info replication，显示：

```
127.0.0.1:6381> info replication
# Replication
role:master
connected_slaves:1
slave0:ip=127.0.0.1,port=6384,state=online,offset=2448,lag=0
master_repl_offset:2448
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:2
repl_backlog_histlen:2447
```

在6382的客户端上执行info replication，显示：

```
127.0.0.1:6382> info replication
# Replication
role:master
connected_slaves:1
slave0:ip=127.0.0.1,port=6385,state=online,offset=2661,lag=0
master_repl_offset:2661
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:2
repl_backlog_histlen:2660
```

可以验证master和slave的对应关系。

现在，可以将6381下线，然后依然在6382的客户获取key1，执行“get key1“，执行结果如下:

```
127.0.0.1:6382> get key1
-> Redirected to slot [9189] located at 127.0.0.1:6384
"1"
127.0.0.1:6384> get key1
"1"
```

可以看到已经将key1迁至6384了，并且可以获取到。此时执行info replication（当前连接的是6384），结果如下：

```
127.0.0.1:6384> info replication
# Replication
role:master
connected_slaves:0
master_repl_offset:0
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0
```

可以看到6384已经升为master了，没有slave。

然后，重新启动6381，控制台终端执行下面语句。

```
redis-server.exe redis.windows-service-6381.conf
```

此时，在6384上，执行info replication，结果如下：

```
127.0.0.1:6384> info replication
# Replication
role:master
connected_slaves:1
slave0:ip=127.0.0.1,port=6381,state=online,offset=15,lag=1
master_repl_offset:15
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:2
repl_backlog_histlen:14
```

可以看到6381已经变成6384的slave了。再执行cluster nodes，结果如下：

```
127.0.0.1:6384> cluster nodes
c906b59f8e13cf52fc22b8843590f661aea39531 127.0.0.1:6382 master - 0 1609903637909 3 connected 10923-16383
940085ce76e0aff7a47a0939bff4ecbdb5017833 127.0.0.1:6385 slave c906b59f8e13cf52fc22b8843590f661aea39531 0 1609903639923 6 connected
2e7d21393bfbfb6f0555a74840452e1925d72128 127.0.0.1:6380 master - 0 1609903641938 1 connected 0-5460
04e87f0a9c7dd3e77979e5e93988d3b8838bf8db 127.0.0.1:6381 slave f10666d4a43966139c6d74efeeb0fe82dbdc4423 0 1609903640931 7 connected
f10666d4a43966139c6d74efeeb0fe82dbdc4423 127.0.0.1:6384 myself,master - 0 0 7 connected 5461-10922
6513356e49986489af2ccd37d9cb369f38107f54 127.0.0.1:6383 slave 2e7d21393bfbfb6f0555a74840452e1925d72128 0 1609903638916 4 connected
```

6个节点都在线，并且6384是master，6381已经变成6384的slave。

注：6个服务的配置文件redis.windows-service-6380.conf、redis.windows-service-6381.conf、redis.windows-service-6382.conf、redis.windows-service-6383.conf、redis.windows-service-6384.conf、redis.windows-service-6385.conf和搭建集群的脚本redis-trib.rb都放在cluster目录下。

因为时间有限，缩阔容的部分没有进行实践，程序对接的部分也没有进行，都是后面几天需要完成的。

最后，本部分的完成，得益于杜宇航同学，推荐网上资源（https://blog.csdn.net/baidu_27627251/article/details/112143714）。特别进行感谢。另外，同时也参考了其他资源，如https://blog.csdn.net/wuxiaolongah/article/details/107306679。非常感谢这些作者们。


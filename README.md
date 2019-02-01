Example of customizing the way `Instant` is processed by customizing a profile 

```
$ sbt run
...
info] Done compiling.
[info] Running Example
Vector(create table "message" ("sender" VARCHAR NOT NULL,"ts_when" BIGINT NOT NULL,"content" VARCHAR,"id" BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT))
Vector(Message(Dave,2019-02-01T19:12:57Z,Some(Hello, HAL. Do you read me, HAL?),1), Message(HAL,2019-02-01T19:12:57Z,Some(Affirmative, Dave. I read you.),2), Message(Dave,2019-02-01T19:12:57Z,Some(Open the pod bay doors, HAL.),3), Message(HAL,2019-02-01T19:12:57Z,Some(I'm sorry, Dave. I'm afraid I can't do that.),4), Message(Dave,2019-02-01T19:12:57Z,None,5))
```




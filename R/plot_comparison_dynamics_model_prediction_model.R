a <- read.csv("test_dist_by_time.log")
par(mfrow=c(1,3))
plot(a$prediction~a$t, main="prediction", col='blue', type="l")
plot(a$dynamics~a$t, main="dynamics", col='red', type="l")
dif <- a$prediction - a$dynamics
plot(dif~a$t, main="difference", type="l")
print(summary(a))
print(summary(dif))

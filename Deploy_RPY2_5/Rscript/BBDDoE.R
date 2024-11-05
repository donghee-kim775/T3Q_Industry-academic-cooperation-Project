# 이 부분은 패키지를 연결시켜주려면 필요한 부분
df <- c()
start_time <- Sys.time()
print(.packages())
# 실행경로 변경 필요(서버에서 R 실행경로를 libPaths로 저장)
.libPaths("C:/Users/DCU/AppData/Local/R/win-library/4.4")

print(.packages())

print('##LIBRARY LOAD _ START##')
# DOE 라이Rcmdr# DOE 라이브러리
re = require(RcmdrPlugin.DoE)

library(Rcmdr)

# encoding 라이브러리
library(base64enc)
print('##LIBRARY LOAD _ END##')

list <- c()
print('create list success')

print(data)

print("##### nrow(data) #####")
print(nrow(data))

for(i in 1:nrow(data)) {
  num_list <- c()
  names(num_list) <- c()
  name <- paste(data['excipients'][i,], sep="")
  range_min = as.numeric(data['input.range.min'][i,])
  range_max = as.numeric(data['input.range.max'][i,])
  num_list <- append(list(c(range_min, range_max)),num_list)
  names(num_list) <- c(name)
  list <- append(num_list, list)
}
print("##### list #####")
print(list)

# if문 생성 : 입력받는 부형제가 1~3개임, 부형제에 개수에 따라 fac.design의 nlevels와 nfactors가 달라짐
if (nrow(data) == 3){
  levels = c(2,2,2)
} else if(nrow(data) == 2){
  levels = c(2,2)
} else {
  levels = c(2)
}

print("##### levels #####")
print(levels)

# step3. list에 들어있는 값을 요인설계법에 적용(기존 있던 코드에서 factor.names만 변경)
print('df start')

# bbd
df.bbd <- bbd.design(nfactors = nrow(data),
                     replications = 1,
                     repeat.only = FALSE,
                     blocks = 1,
                     randomize = FALSE,
                     seed = 29408,
                     nlevels = levels,
                     factor.names = list)

print("##### DF.BBD #####")
print(df.bbd)

print("##### DF.BBD_DOE #####")
df.bbd_DoE <- as.data.frame(df.bbd)
print(df.bbd_DoE)

print("##### BBDDOE_END #####")

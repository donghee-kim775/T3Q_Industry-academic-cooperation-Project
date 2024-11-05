data  <- read.csv("./DATA/API5.csv")
data

list <- c()

for(i in 1:nrow(data)) {
  num_list <- c()
  names(num_list) <- c()
  name <- paste(data['excipients'][i,], sep="")
  range_min = data['input.range.min'][i,]
  range_max = data['input.range.max'][i,]
  num_list <- append(list(c(range_min, range_max)),num_list)
  names(num_list) <- c(name)
  list <- append(num_list, list)
}

re = require(RcmdrPlugin.DoE)

if (nrow(data) == 3){
  levels = c(2,2,2)
} else if(nrow(data) == 2){
  levels = c(2,2)
} else {
  levels = c(2)
}

df <- fac.design(nfactors = nrow(data),
                 replications = 1,
                 repeat.only = FALSE,
                 blocks = 1,
                 randomize = FALSE,
                 seed = 29408,
                 nlevels = levels,
                 # if문 추가
                 factor.names = list)

df.centerpts <- add.center(df, ncenter = 5, distribute = 1)

# step5. 중심점에 따른 중앙 복합 설계
df.ccd <- ccd.augment(df.centerpts,
                      alpha = 1,
                      ncenter = c(1),
                      randomize = FALSE,
                      seed = 28054)
# Finally the result was followed ("D:/data/result/test.csv')
df.ccd
decode.data(df.ccd)
write.csv(df.ccd, file="./DATA/API5ouput.csv")

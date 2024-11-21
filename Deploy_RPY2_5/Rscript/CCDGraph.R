# library(jsonlite)
# json_data <- '{"excipients": ["Acetic Acid", "Citric Acid"],"input.range.min": [1, 2],"input.range.max": [12, 24]}'
# parseddata <- fromJSON(json_data)
# data <- as.data.frame(parseddata)
# data

# df.response <- data.frame(disintegration = c(3, 2, 7, 4, 5, 11, 7, 8, 5, 4, 17, 12, 13, 14),
#                         hardness = c(12, 7, 9, 10, 4, 7, 3, 2, 4, 3, 2, 1, 6, 1))
# df.response
# 이 부분은 패키지를 연결시켜주려면 필요한 부분
df <- c()
start_time <- Sys.time()
print(.packages())
# 실행경로 변경 필요(서버에서 R 실행경로를 libPaths로 저장)
.libPaths("C:/Users/DCU/AppData/Local/R/win-library/4.4")

print(.packages())
# DOE 라이Rcmdr# DOE 라이브러리
re = require(RcmdrPlugin.DoE)

library(Rcmdr)

# encoding 라이브러리
library(base64enc)

(.packages())

# step1 ~ 2. csv에서 excipients와 최소값, 최대값 list 저장
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

# if문 생성 : 입력받는 부형제가 1~3개임, 부형제에 개수에 따라 fac.design의 nlevels와 nfactors가 달라짐
if (nrow(data) == 3){
  levels = c(2,2,2)
} else if(nrow(data) == 2){
  levels = c(2,2)
} else {
  levels = c(2)
}
levels

# step3 list에 들어있는 값을 요인설계법에 적용(기존 있던 코드에서 factor.names만 변경)
df <- fac.design(nfactors = nrow(data), # 요인의 수
                 replications = 1, # 실험 반복 횟수
                 repeat.only = FALSE, # 요인 수준 제어 False : 모든 요인 수준의 조합 포함
                 blocks = 1,
                 randomize = FALSE, # 실험순서 무작위
                 seed = 29408, # 난수 생성
                 nlevels = levels, # 각 요인의 수준
                 factor.names = list)
print(df)

# step4 기존 있던 소스(중심점 추가, min, max에 따른 중심점 추가)
df.centerpts <- add.center(df, ncenter = 5, distribute = 1)
print(df.centerpts)

# step5. 축점의 거리 / 중심점에 따른 중앙 복합 설계
df.ccd <- ccd.augment(df.centerpts,
                      alpha = 1, # 중심점에서 축점까지의 거리 비율 
                      ncenter = c(1), # 중심점의 수
                      randomize = FALSE,
                      seed = 28054)
df.ccd

## step4. 그래프 생성을 위한 rsm.models 생성 과정
df.ccd.response <- add.response(df.ccd, df.response, replace = FALSE)
df.ccd.response

df.ccd.response.coded <- code.design(df.ccd.response)
print("###### Central Composite Design ######")
print(df.ccd.response.coded)

# num.x = x 요소개수
# # num.y = y 요소 개수
# xvars = (excipients들의 개수에 따라서 3개이면)x1, x2, x3 생성
# yvars = y의 명 (ex. disintegration, hardness)
# ', ' 하나의 string으로 저장
num.x <- ncol(df)
num.y <- ncol(df.response)
print(num.x)
print(num.y)
xvars <- colnames(df.ccd.response.coded)[2:(num.x+1)]
yvars <- colnames(df.response)
print(xvars)
print(yvars)

xvars.comma <- paste(xvars, collapse = ", ")
yvars.comma <- paste(yvars, collapse = ", ")

print("##### RSM 모델 생성 #####")
# rsm : 반응 표면 회귀, 반응 표면 성분으로 선형 모델을 피팅하고 적절한 분석과 요약을 생성
rsm.models <- list()
for (yvar in yvars) {
  # make formula for each response variable
  # ex. disintegration ~ FO(x1, x2, x3)
  # @@@ 수정 필요 이거는 x값을 3개를 받을 때를 고정시킨 값 if문 돌려야할듯
  xformula <- paste("FO(", xvars.comma, ") + TWI(", xvars.comma, ") + PQ(", xvars.comma, ")")
  
  # yvars와 xformula를 ~을 기준으로 합친다.
  str.formula <- paste(yvar, xformula, sep = " ~ ")
  
  # formula 함수 : 다른 개체에 포함된 수식을 추출하는 방법 제공, rsm,formula : "str.formula" -> str.formula
  rsm.formula <- as.formula(str.formula)
  
  # rsm.formula를 컬럼명으로 하고 data를 df.ccd.response.coded로 진행
  rsm.model <- rsm(rsm.formula, data = df.ccd.response.coded)
  rsm.models <- append(rsm.models, list(rsm.model))
}
print("##### RSM 모델 생성 완료 #####")

print("##### RSM-ANOVA #####")
#########################
######### anova #########
#########################
anova_df_list <- list()

# 데이터프레임 생성하는 부분은 동일
for (i in 1:length(rsm.models)){
  response_type <- as.character(rsm.models[[i]]$call$formula)[2]
  rsm_anova <- anova(rsm.models[[i]])
  anova_df <- as.data.frame(rsm_anova)
  anova_df$response_type <- response_type

  # row.names를 source라는 새로운 컬럼으로 저장
  anova_df$source <- rownames(anova_df)
  # row.names 초기화
  rownames(anova_df) <- NULL
  print(anova_df) 
  anova_df_list[[i]] <- anova_df
}

# 데이터프레임 결합
combined_anova_df <- do.call(rbind, anova_df_list)

# 뒤에 붙는 숫자 제거를 위해 source 컬럼에서 숫자 제거
combined_anova_df$source <- gsub("[0-9]", "", combined_anova_df$source)

# 필요한 컬럼 선택 및 NA 값을 0으로 대체
final_anova_df <- combined_anova_df[, c("response_type", "source", "Df", "Sum Sq", "Mean Sq", "F value", "Pr(>F)")]
final_anova_df[is.na(final_anova_df)] <- 0

# 컬럼 이름 변경
colnames(final_anova_df) <- c( "response_type", "source","DF", "sumsq", "meansq", "F_VALUE", "PR(>F)")

# 결과 출력
print("##### ANOVA 결과 #####")
print(final_anova_df)
#############################
######### Anova_End #########
#############################

# 이미지명 x,y 조합을 위해 y_list 추출 필요
y_list <- names(df.response)

# add
y_list <- gsub("[[:punct:]]", " ", y_list)
df_y <- data.frame(y_list)

# image_to_text text 담을 데이터프레임
image_df <- data.frame(image_type = character(), image_name = character(), image_to_text = character(), stringsAsFactors = FALSE)
image_df

#################################
###---Contour_Graph_DF 생성---###
#################################
## x의 개수가 2개인 경우
print("#################")
print("##Contour_Graph##")
print("#################")
if (nrow(data) == 2){
  print("22222")
  for (i in 1:length(rsm.models)){
    image_name = paste(data['excipients'][[1]][[1]],"_",data['excipients'][[1]][[2]],"_",df_y[[1]][[i]], "contour")
    image_path1 <- tempfile(image_name, fileext = ".png")
    cat("contour 저장 path : ", image_path1)
    
    png(filename = image_path1)
    contour(rsm.models[[i]], as.list(c(~ x1 * x2)), image = TRUE, atpos = 1)
    dev.off()
    
    enc_image1 <- base64enc::base64encode(image_path1)
    image_df <- rbind(image_df, data.frame(image_type = "contour", image_name = image_name, image_to_text = enc_image1))
  }
  ## x의 개수가 3개인 경우
} else if (nrow(data) == 3) {
  print("333333")
  ## excipients 2번째, 3번째 값
  for (i in 1:length(rsm.models)){
    image_name = paste(data['excipients'][[1]][[2]],"_",data['excipients'][[1]][[3]],"_",df_y[[1]][[i]], "contour")
    image_path1 <- tempfile(image_name, fileext = ".png")
    cat("contour_1 저장 path : ", image_path1)
    
    png(filename = image_path1)
    contour(rsm.models[[i]], as.list(c(~ x2 * x1)), image = TRUE, atpos = 1)
    dev.off()
    
    enc_image1 <- base64enc::base64encode(image_path1)
    image_df <- rbind(image_df, data.frame(image_type = "contour", image_name = image_name, image_to_text = enc_image1))
  }
  ## excipients 2번째, 1번째 값
  for (j in 1:length(rsm.models)){
    image_name2 = paste(data['excipients'][[1]][[1]],"_",data['excipients'][[1]][[2]],"_",df_y[[1]][[j]], "contour")
    image_path2 <- tempfile(image_name2, fileext = ".png")
    cat("contour_2 저장 path : ", image_path2)
    
    png(filename = image_path2)
    contour(rsm.models[[j]], as.list(c(~ x3 * x2)), image = TRUE, atpos = 1)
    dev.off()
    
    enc_image2 <- base64enc::base64encode(image_path2)
    image_df <- rbind(image_df, data.frame(image_type = "contour", image_name = image_name2, image_to_text = enc_image2))
  }
  ## excipients 3번째, 1번째 값
  for (z in 1:length(rsm.models)){
    image_name3 = paste(data['excipients'][[1]][[1]],"_",data['excipients'][[1]][[3]],"_",df_y[[1]][[z]], "contour")
    image_path3 <- tempfile(image_name3, fileext = ".png")
    cat("contour_3 저장 path : ", image_path3)
    
    png(filename = image_path3)
    contour(rsm.models[[z]], as.list(c(~ x3 * x1)), image = TRUE, atpos = 1)
    dev.off()
    
    enc_image3 <- base64enc::base64encode(image_path3)
    image_df <- rbind(image_df, data.frame(image_type = "contour", image_name = image_name3, image_to_text = enc_image3))
  }
  ## x의 개수가 2개도 3개도 아닐때
} else {
  print("error")
}

##################################
###---Response_Graph_DF 생성---###
##################################
## x의 개수가 2개인 경우
print("##################")
print("##Response_Graph##")
print("##################")
if (nrow(data) == 2){
  print("22222")
  for (i in 1:length(rsm.models)){
    image_name1 = paste(data['excipients'][[1]][[1]],"_",data['excipients'][[1]][[2]],"_",df_y[[1]][[i]], "Response")
    image_path1 <- tempfile(image_name1, fileext = ".png")
    cat("Response path : ", image_path1)
    
    png(filename = image_path1)
    persp(rsm.models[[i]], as.list(c(~ x1 * x2)), zlab = y_list[i], contours = "col", col = rainbow(50, end = 5 / 6), atpos = 1)
    dev.off()
    
    enc_image1 <- base64enc::base64encode(image_path1)
    image_df <- rbind(image_df, data.frame(image_type = "response", image_name = image_name1, image_to_text = enc_image1))
  }
  ## x의 개수가 3개인 경우
} else if (nrow(data) == 3) {
  print("333333")
  ## excipients 2번째, 3번째 값
  for (i in 1:length(rsm.models)){
    image_name1 = paste(data['excipients'][[1]][[2]],"_",data['excipients'][[1]][[3]],"_",df_y[[1]][[i]], "Response")
    image_path1 <- tempfile(image_name1, fileext = ".png")
    cat("contour_1 path : ", image_path1)
    
    png(filename = image_path1)
    persp(rsm.models[[i]], as.list(c(~ x2 * x1)), zlab = y_list[i], contours = "col", col = rainbow(50, end = 5 / 6), atpos = 1)
    dev.off()
    
    enc_image1 <- base64enc::base64encode(image_path1)
    image_df <- rbind(image_df, data.frame(image_type = "response", image_name = image_name1, image_to_text = enc_image1))
  }
  ## excipients 2번째, 1번째 값
  for (j in 1:length(rsm.models)){
    image_name2 = paste(data['excipients'][[1]][[1]],"_",data['excipients'][[1]][[2]],"_",df_y[[1]][[j]], "Response")
    image_path2 <- tempfile(image_name2, fileext = ".png")
    cat("contour_2 path : ", image_path2)
    
    png(filename = image_path2)
    persp(rsm.models[[j]], as.list(c(~ x3 * x2)), zlab = y_list[j], contours = "col", col = rainbow(50, end = 5 / 6), atpos = 1)
    dev.off()
    
    enc_image2 <- base64enc::base64encode(image_path2)
    image_df <- rbind(image_df, data.frame(image_type = "response", image_name = image_name2, image_to_text = enc_image2))
  }
  ## excipients 3번째, 1번째 값
  for (z in 1:length(rsm.models)){
    image_name3 = paste(data['excipients'][[1]][[1]],"_",data['excipients'][[1]][[3]],"_",df_y[[1]][[z]], "Response")
    image_path3 <- tempfile(image_name3, fileext = ".png")
    cat("contour_3 path : ", image_path3)
    
    png(filename = image_path3)
    persp(rsm.models[[z]], as.list(c(~ x3 * x1)), zlab = y_list[z], contours = "col", col = rainbow(50, end = 5 / 6), atpos = 1)
    dev.off()
    
    enc_image3 <- base64enc::base64encode(image_path3)
    image_df <- rbind(image_df, data.frame(image_type = "response", image_name = image_name3, image_to_text = enc_image3))
  }
  ## x의 개수가 2개도 3개도 아닐때
} else {
  print("error")
}

##################################
###----pareto_Graph_DF 생성----###
##################################
# ==> Pareto Chart
print("################")
print("##Pareto_Graph##")
print("################")
for (i in 1:length(rsm.models)){
  image_path1 <- tempfile(fileext = ".png")
  cat("pareto_1 path : ", image_path1)
  
  png(filename = image_path1)
  qqnorm(rstandard(rsm.models[[i]]), ylab="Standardized Residuals", xlab="Normal Scores", main="Normal Probability Plot")
  qqline(rstandard(rsm.models[[i]]))
  dev.off()
  
  enc_image1 <- base64enc::base64encode(image_path1)
  image_name = paste("Y", i, "_", df_y[[1]][[i]], "pareto")
  image_df <- rbind(image_df, data.frame(image_type = "pareto", image_name = image_name, image_to_text = enc_image1))
}

print(image_df)
print("#############")
print("R Program End")
print("#############")
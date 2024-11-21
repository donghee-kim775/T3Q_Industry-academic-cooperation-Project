<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="CacheCommboUtil" uri="/WEB-INF/tlds/CacheCommboUtil.tld"%>

<%@ include file="/WEB-INF/jsp/egovframework/mordern/config/common.jsp"%>
<script type="text/javascript">
	//<![CDATA[
		$(document).ready(function(e){

			var primaryDtl = {
					'value' : '<%=param.getString("value")%>',
					'unit' : '<%=param.getString("unit")%>'
					}
			var param = {
					'smiles' : '<%=param.getString("smiles")%>',
					'Pharmaceutical Dosage Form' : '<%=param.getString("formulation") %>',
					'primary' : primaryDtl,
					};
			
			//if(step_new 가 Y일때)
			<%if(param.getString("step_new") == "Y"){%>
			
				var smiles = '';
				var route = '';
				var formulation = '';
				var paramSimilarity = '';
				
				$.ajax({
					url: "/pharmai/formulation/getApi3Ajax.do",
					type: 'post',
					datatType: 'json',
					data: param,   //inputData: return smiles, formulation: ex)Capsule, Oral Capsule , volume: ex)12
					async : false,
					success: function(response){
						var code = response.resultStats.resultData.code;
						if(code == "000"){
							var data = response.resultStats.resultData;
							
							// 실제 코드 (db 서비스 호출 시에 들어갈 새로운 param 값 가져오기)
							smiles = data.result["ai-based"][0]["reference SMILES"];
							route = data.request["Route of Administration"];
							formulation = data.request["Pharmaceutical Dosage Form"];
							paramSimilarity = data.result["ai-based"][0]["similarity"];
						}else{
							var prjct_id = $('input[name=prjct_id]').val();
							var msg = response.resultStats.resultData.msg;
							var code = response.resultStats.resultData.code;
							var redirectUrl = "/pharmai/chemical/formulation/";
							

							switch (code){
								case '001':
								case '002':
								case '005':
								case '008':
								case '009':
								case '999':
									redirectUrl += "selectRoutes.do?prjct_id="+prjct_id;
									break;

							}
							alert(msg);
							location.href = redirectUrl;
						}
					},
					error : function(jqXHR, textStatus, thrownError){
						ajaxJsonErrorAlert(jqXHR, textStatus, thrownError)
					}
				});
				
				
				var param = {       
				    'smiles' : smiles,        
					'route' : route,     
					'formulation' : formulation,      
					'similarity' : paramSimilarity    
					};
				
				// db 조회
				$.ajax({
					url : '/pharmai/formulation/selectListFormulationComboAjax.do',
					type: 'post',
					datatType: 'json',
					data: param,   
					async : false,
					success: function(response){
						console.log('response check : ', response);
						var resultList = response.resultStats.list;
						var tag ='';
						for(var i = 0; i < resultList.length; i++){
							const keys = Object.keys(resultList[i]);
							const values = Object.values(resultList[i]);
							
							const indices = [];
							
							// 추천 구분 값 변경 필요
							// General 추천 -> General | 제형별 추천 -> Route of Admin / Dosage Form | SMILES 기반 (AI)추천 -> SMILES similarity
							if (resultList[i]["recom_type"] == 'General 추천'){
								resultList[i]["recom_type"] = 'General';
							} else if (resultList[i]["recom_type"] == '제형별 추천') {
								resultList[i]["recom_type"] = 'Route of Admin / Dosage Form';
							} else if (resultList[i]["recom_type"] == 'SMILES 기반 (AI)추천') {
								resultList[i]["recom_type"] = 'SMILES similarity';
							}

							// 빈값은 '-' 처리
							if (values.includes('')) {								
								let idx = values.indexOf('');							
								while (idx != -1) {
									indices.push(idx);
									idx = values.indexOf('', idx + 1);
								}			
								// 값 바꾸기			
								for (let j = 0; j < indices.length; j++) {
									const keyName = keys[indices[j]];
									resultList[i][keyName] = '-';
								}
							}
								
							tag += '<tr>';
							tag += '	<td><input type="checkbox" name="filter" value="N"></td>';
							tag += '	<td>' + resultList[i].recom_type + '</td>';
							tag += '	<td>' + resultList[i].reliability + '</td>';
							tag += '	<td>' + resultList[i].kind + '</td>';
							tag += '	<td>' + resultList[i].excipient  +'</td>';
							tag += '	<td>' + resultList[i].use_range +'</td>';
							tag += '	<td>' + resultList[i].max_amount  +'</td>';
							tag += '	<td><input type="text" class="text-center" name="startRange"  value="" size="3" readonly /> ~ <input type="text" class="text-center" name="endRange" value="" size="3" readonly /></td>'
							tag += '	<input type="hidden" name="kind" value= "' + resultList[i].recom_type + '">';
							tag += '	<input type="hidden" name="use_range_s" value= "' + resultList[i].reliability + '">';
							tag += '	<input type="hidden" name="unit" value= "' + resultList[i].kind + '">';
							tag += '	<input type="hidden" name="excipients" value="' + resultList[i].excipient + '">';
							tag += '	<input type="hidden" name="excipient" value="' + resultList[i].use_range + '">';
							tag += '	<input type="hidden" name="maximum" value="' + resultList[i].max_amount + '">';
							tag += '	<input type="hidden" name="checkYn" value="N">';
							tag += '</tr>';
						}
						
						$('#renderExcipient').append(tag);
						return;
					},error : function(jqXHR, textStatus, thrownError){
						ajaxJsonErrorAlert(jqXHR, textStatus, thrownError)
					}
				});
			<%} %>

			//체크박스 클릭시 disable삭제
			$("input[name=filter]").click(function(){

				var count = $("input:checked[name='filter']").length;

				if(count > 3){
					$(this).prop("checked", false);
					alert("선택은 3개까지만 할 수 있습니다.");
					return;
				}

				var chk = $(this).is(":checked");

				var startRange = $(this).closest('tr').find("input[name=startRange]");
				var endRange = $(this).closest('tr').find("input[name=endRange]");
				var filter = $(this).closest('tr').find("input[name=filter]");
				var checkYn = $(this).closest('tr').find("input[name=checkYn]");

				if(chk){
					filter.val("Y");
					checkYn.val("Y");
					startRange.prop("readonly", false);
					endRange.prop("readonly", false);
					//빈값 입력 안할 시 focus 를 위한 class 추가
					startRange.attr('class', 'readonlyFalse text-center');
					endRange.attr('class', 'readonlyFalse text-center');
				}else{
					filter.val("N");
					checkYn.val("N");
					startRange.prop("readonly", true);
					endRange.prop("readonly", true);
					//빈값 입력 안할 시 focus 를 위한 class 추가
					startRange.attr('class', 'readonlyTrue');
					endRange.attr('class', 'readonlyTrue');

					for(var i = 0; i< startRange.length; i++){
					 	startRange[i].value = "";
						endRange[i].value = "";
					}
				}


			});

		});


	//]]>
</script>

<jsp:include page="headerStep.jsp" flush="false"/>

<div class="content">
	<div class="container-fluid">
		<div class="row">
			<div class="col-lg-12">
				<form role="form" id="aform" name="aform" method="post" action="">
					<input type="hidden" id="status" name="status" value="03"/>
					<input type="hidden" name="return_smiles" value="">
					<input type="hidden" name="step_new" value="<%=param.getString("step_new")%>"/>
					<input type="hidden" name="next_data" value="<%=param.getString("next_data")%>"/>
					<input type="hidden" name="prjct_type" value="<%=param.getString("prjct_type")%>"/>
					<input type="hidden" name="prjct_id" value="<%=param.getString("prjct_id")%>"/>
					<input type="hidden" name="projectName" value ="">
					<input type="hidden" name="return_smailes" value="<%=param.getString("smiles")%>">
					<input type="hidden" name="stp_02_seq"  value="<%=param.getString("stp_02_seq")%>">

					<div class="card card-info card-outline">
						<div class="card-header">
							<h4 class="card-title">Excipient Input</h4>
						</div>
						<div class="card-body">
							<div class="table-responsive">
								<table id="excipientTable" class="table text-nowrap table-hover">
									<thead>
										<tr>
											<th>선택</th>
											<th>추천 구분</th>
											<th>신뢰도</th>
											<th>부형제 유형</th>
											<th>부형제</th>
											<th>사용 범위</th>
											<th>최대 사용 가능량</th>
											<th>사용 예정 범위 입력</th>
										</tr>
									</thead>

									<c:if test="${resultList == null }">
										<tbody id="renderExcipient"></tbody>
									</c:if>

									<c:if test="${resultList != null }">
										<tbody>
											<c:forEach var="item" items="${resultList }" varStatus="status">
												<tr>
													<td>
														<c:if test="${item.CHECK_YN == 'Y' }">
															<input type="checkbox" name="filter" value="${item.CHECK_YN }" checked>
														</c:if>
														<c:if test="${item.CHECK_YN == 'N' }">
															<input type="checkbox" name="filter" value="${item.CHECK_YN }">
														</c:if>

													</td>
													<td>${item.KIND}</td> <!-- 추천구분 -->
													<td>${item.USE_RANGE_S}</td> <!-- 신뢰도 -->
													<td>${item.UNIT }</td> <!-- 부형제 유형 -->
													<td>${item.EXCIPIENT }</td> <!-- 부형제 -->
													<td>${item.USE_RANGE_E }</td> <!-- 사용 범위 -->
													<td>${item.MAXIMUM }</td> <!-- 최대 사용 가능량 -->
													<td>
														<c:if test="${item.CHECK_YN == 'Y' }">
															<input type="text"  name="startRange" class="text-center"  value="${item.IPT_USE_RANGE_S }" size="3" /> ~ <input type="text" class="text-center" name="endRange" value="${item.IPT_USE_RANGE_E }" size="3" />
														</c:if>

														<c:if test="${item.CHECK_YN == 'N' }">
															<input type="text"  name="startRange" class="text-center" value="${item.IPT_USE_RANGE_S }" size="3" readonly /> ~ <input type="text" class="text-center" name="endRange" value="${item.IPT_USE_RANGE_E }" size="3" readonly />
														</c:if>
													</td>
													<!-- tr 안에서 동작. -->
													<input type="hidden" name="kind" value="${item.KIND }">
													<input type="hidden" name="excipients" value="${item.EXCIPIENT }">
													<input type="hidden" name="excipient" value="${item.USE_RANGE_E }">
													<input type="hidden" name="use_range_s" value="${item.USE_RANGE_S }">
													<input type="hidden" name="maximum" value="${item.MAXIMUM }">
													<input type="hidden" name="unit" value="${item.UNIT }">
													<input type="hidden" name="checkYn" value="${item.CHECK_YN }">

												</tr>

											</c:forEach>
										</tbody>
									</c:if>
								</table>
							</div>
						</div>
					</div>
				</form>
			</div>
		</div>
	</div>
</div>


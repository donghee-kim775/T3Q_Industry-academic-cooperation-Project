<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"     uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="Util" uri="/WEB-INF/tlds/Util.tld"%>

<jsp:useBean id="cqasList"  type="java.util.List" class="java.util.ArrayList" scope="request"/>

<%@ include file="/WEB-INF/jsp/egovframework/mordern/config/common.jsp" %>

<script type="text/javascript">
    $(document).ready(function() {
        // Handle Design of Experiment selection change
        $('#DoE').change(function() {
            var selectedValue = $(this).val();

            // Show total-volume input if SLD is selected, hide otherwise
            if (selectedValue === 'SLD') {
                $('#total-volume-container').show(); // Show the input field
            } else {
                $('#total-volume-container').hide(); // Hide the input field
            }
        });
    });
</script>

<script type="text/javascript">
//<![CDATA[

	$(document).ready(function(e){
		var step_new = $('input[name=step_new]').val();
		var cqa_nm = $('input[name=cqa_nm]').val();
		var cqa_routes = $('input[name=cqa_routes]').val();
		var cntExcipient = <%= param.getInt("cntExcipient") %>;
		var param = {
				'formulation' : cqa_nm,
				'routes' : cqa_routes,
		}

		//checkbox max ten
		$("input[type='checkbox']").on("click", function(){
			var count = $("input:checked[type='checkbox']").length;

			if(count > 11){
				$(this).prop("checked", false);
				alert("선택은 10개까지만 할 수 있습니다.");
			}
		});
		
		$('#DoE').change(function() {
	        var cntExcipient = $('input[name=cntExcipient]').val(); // hidden input에서 cntExcipient 값 가져옴

	        // BBD 선택 시 cntExcipient 값이 3 미만일 때 경고
	        if ($(this).val() === 'BBD' && cntExcipient < 3) {
	            alert('Excipient가 3개 미만일 때는 Box-Behnken Design (BBD)을 선택할 수 없습니다.');
	            $(this).val("none"); // 선택 초기화
	        }
	    });
		
		if(step_new == 'Y'){
			console.log("url : getApi4RoutesAjax");
			$.ajax({
				//url : '/pharmai/formulation/getApi4Ajax.do',
				url : '/pharmai/formulation/getApi4RoutesAjax.do',
				type : 'post',
				dataType : 'json',
				data : param,
				async : false,
				success: function(response){
					//console.log("success response : ", response);

					var redirectUrl = "";

					
					var data = response;
					var cqasList = data.CQAs;
					//var cqas = cqasList[0].split(',');

					var innerHtml = '';

					for(var i = 0; i < cqasList.length; i++){
						var cqa_nm = cqasList[i].cqa_eng;

						innerHtml += '<tr class="">';
						innerHtml += '	<td class="checkbox_outline"><input type="checkbox" name="cqas_list" value="'+ cqa_nm + '"></td>';
						innerHtml += '	<td class="text_outline"><span>' + cqa_nm + '</span><input type="hidden" name="cqas_name" value="' + cqa_nm + '"></td>';
						innerHtml += '</tr>';
					}
					$('#cqasTable tbody').append(innerHtml);

				},
				error : function(jqXHR, textStatus, thrownError){
					ajaxJsonErrorAlert(jqXHR, textStatus, thrownError)
				}

			});
		}

	});

//]]>
</script>

<jsp:include page="headerStep.jsp" flush="false"/>

<div class="content">
	<div class="container-fluid">
		<form role="form" id="aform" name="aform" method="post" action="">
			<input type="hidden" id="status" name="status" value="04"/>
			<input type="hidden" name="return_smiles" value="">
			<input type="hidden" name="step_new" value="<%=param.getString("step_new")%>"/>
			<input type="hidden" name="next_data" value="<%=param.getString("next_data")%>"/>
			<input type="hidden" name="prjct_type" value="<%=param.getString("prjct_type")%>"/>
			<input type="hidden" name="prjct_id" value="<%=param.getString("prjct_id")%>"/>
			<input type="hidden" name="projectName" value ="">
			<input type="hidden" name="cqa_nm" value="<%=param.getString("cqa_nm")%>"/>
			<input type="hidden" name="cqa_routes" value="<%=param.getString("cqa_routes")%>"/>
			<input type="hidden" name="cntExcipient" value="<%=param.getInt("cntExcipient")%>">
						<div class = "row">
				<%--실험 설계법 row--%>
					<div class="col-lg-12">
						<div class="card card-outline">
							<div class="card-header">
								<h4 class="card-title">실험설계법(Design Of Experiment) 선택</h4>
							</div>
							<div class = "card-body">
								<div class="form-group">
									<label for = "DOESelection">Design Of Experiment 방식</label>
									
									<div class = "input-group">
										<select class = "select form-control inputType" id ="DoE" name ="DoE">
											<option value = "none"> 실험설계법 방식 선택</option>
											<option value = "CCD">Response Surface Design : Central Composite Design</option>
											<option value = "BBD">Response Surface Design : Box-Behnken Design</option>
											<option value = "SLD">Mixture Design : Simplex Lattice Design</option>
										</select>
									</div>
									
						            <!-- Total Volume Input Field -->
						            <div class="row" id="total-volume-container" style="display:none;">
						                <div class="col-lg-12">
						                    <div class="card card-outline">
						                        <div class="card-body">
						                            <div class="form-group">
						                                <label for="total-volume">Total Volume:</label>
						                                <div class="input-group">
						                                    <input type="text" class="form-control" id="total-volume" name="total-volume" placeholder="Enter total volume">
						                                </div>
						                            </div>
						                        </div>
						                    </div>
						                </div>
						            </div>
								</div>
							</div>
						</div>
					</div>
			</div>
			<%--#################--%>
			<div class="row">
				<div class="col-lg-12">
					<div class="card card-outline">
						<div class="card-header">
							<h4 class="card-title">CQAs List</h4>
						</div>
						<div class="card-body">
							<div class="table-responsive">
								<table class="table text-nowrap table-hover" id="cqasTable">
								<thead>
									<tr>
										<th></th>
										<th>CQAs List</th>
									</tr>
								</thead>
								<tbody>

									<%
										DataMap cqasMap = null;
										for(int i = 0; i < cqasList.size(); i++){
											cqasMap = (DataMap)cqasList.get(i);
									%>
										<tr class="">
											<td class="checkbox_outline"><input type="checkbox" name="cqas_list" value="<%=cqasMap.getString("CQA_NM") %>" <%if(cqasMap.getString("CHECK_YN").equals("Y")){%> checked="checked" <%}%>></td>
											<td class="text_outline"><span><%=cqasMap.getString("CQA_NM")%></span><input type="hidden" name="cqas_name" value="<%=cqasMap.getString("CQA_NM")%>"></td>
										</tr>
									<%
										}
									%>
								</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
			</div>
		</form>
	</div>
</div>



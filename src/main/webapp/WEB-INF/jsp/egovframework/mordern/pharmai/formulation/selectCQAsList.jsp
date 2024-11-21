<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"     uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="Util" uri="/WEB-INF/tlds/Util.tld"%>

<jsp:useBean id="cqasList"  type="java.util.List" class="java.util.ArrayList" scope="request"/>

<%@ include file="/WEB-INF/jsp/egovframework/mordern/config/common.jsp" %>

<script type="text/javascript">
//<![CDATA[

	$(document).ready(function(e){
		var step_new = $('input[name=step_new]').val();
		var cqa_nm = $('input[name=cqa_nm]').val();
		var cqa_routes = $('input[name=cqa_routes]').val();

		var param = {
				'formulation' : cqa_nm,
				'routes' : cqa_routes
		}

		//checkbox max ten
		$("input[type='checkbox']").on("click", function(){
			var count = $("input:checked[type='checkbox']").length;

			if(count > 11){
				$(this).prop("checked", false);
				alert("선택은 10개까지만 할 수 있습니다.");
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



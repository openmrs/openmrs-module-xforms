<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<style>
 .descriptionBox {
 	border-color: transparent;
 	border-width: 1px;
 	overflow-y: auto;
 	background-color: transparent;
 	padding: 1px;
 	height: 2.7em;
 }
 td.description {
 	padding-top: 0px;
 }
 #buttonsAtBottom {
 	padding: 5px;
 }
</style>


<h2><spring:message code="xforms.xformProperties" /></h2>



<form method="post" enctype="multipart/form-data" class="box" onsubmit="removeHiddenRows()">
	
	
	<table>
		<thead>
			<tr>
				<th><spring:message code="general.name" /></th>
				<th><spring:message code="general.value" /></th>
				<th></th>
			</tr>
		</thead>
			<c:forEach var="xformsProp" items="${xformsProps}">
			<tr>
			
				<td><input type = "text" name = "property" value = "${xformsProp.property}" size="50" maxlength="250"/></td>
				<td valign="top">
						<c:choose>
							<c:when test="${fn:length(xformsProp.propertyValue) > 20}">
								<textarea name="value" onchange="edited()" rows="1" cols="60" wrap="off">${xformsProp.propertyValue}</textarea>
							</c:when>
							<c:otherwise>
								<input type="text" name="value" value="${xformsProp.propertyValue}" size="30" maxlength="4000" onchange="edited()" />
							</c:otherwise>
						</c:choose>
					</td>
				
			</tr>
				<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
					<td colspan="2" valign="top" class="description">
						<textarea name="description" class="descriptionBox" 
							rows="2" cols="96" onchange="edited()"
							onfocus="descriptionFocus(this)" onblur="descriptionBlur(this)">${xformsProp.description}</textarea>
					</td>
				</tr>
			
			
			</c:forEach>
			
			
	</table>
	
	<span id="buttonsAtBottom">
		<input type="submit" name="action" value='<spring:message code="general.save"/>' />
		&nbsp;&nbsp;&nbsp;&nbsp;
		<input type="submit" name="action" value='<spring:message code="general.cancel"/>' />
	</span>
</form>
	


<%@ include file="/WEB-INF/template/footer.jsp"%>
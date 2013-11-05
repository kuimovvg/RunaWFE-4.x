
<style>
table.grayed {
	width: 100%;
}

table.grayed tr {
	border-bottom: 1px solid #888888;
}

table.grayed tr td {
	border-top: 1px solid #666666;
	padding: 5px;
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10pt;
}

table.grayed caption {
	background-color: #eeeeee;
	font-weight: bold;
	text-align: center;
}

.inputString {
	width: 100%;
}

.inputText {
	width: 100%;
	height: 50px;
}

</style>

<table class="grayed">

<#list variables as variable>   
	<tr>
		<td>${variable.name}</td>
		<td>${variable.tag}</td>
	</tr>
</#list>

</table>
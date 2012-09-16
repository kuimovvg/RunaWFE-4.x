<?xml version="1.0" encoding="utf-8"?>
<x:stylesheet
 	version="1.0"
	xmlns:x="http://www.w3.org/1999/XSL/Transform"
>
	<x:output method="xml" indent="yes"/>
	
	<x:param name="build.number"/>
	
	<x:template match="text()|@*|*|processing-instruction()|comment()" priority="-1">
        <x:copy>
			<x:apply-templates select="text()|@*|*|processing-instruction()|comment()"/>
        </x:copy>
    </x:template>
    <x:template match="td[@id = concat('ss-',$build.number)]">
    	<x:copy>
 			<x:apply-templates select="@*|*|processing-instruction()|comment()"/>
 			BUILD
    	</x:copy>
    </x:template>
</x:stylesheet>
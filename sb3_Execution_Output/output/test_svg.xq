declare namespace xlink="http://www.w3.org/1999/xlink";
declare namespace svg = "http://www.w3.org/2000/svg";

let $n := doc("sb3_Execution_Output/output/transitions.svg")//g/title/text()

return
<title>
{count($n)}
</title> 
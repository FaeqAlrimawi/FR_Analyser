declare namespace cyberPhysical_Incident = "http://www.example.org/cyberPhysical_Incident";
declare namespace environment = "http://www.example.org/environment";

 
(:
:: This function compares the number and types of connections found in an incident asset to that of a space asset
:: Returns true if they match according to some conditions (e.g., connectionsKnowledge be "exact"), false otherwise
:)
declare function local:compare-connections($incidentConns as xs:string?, $connectionsKnowledge as xs:string?, 
$spaceConns as xs:string?, $incidentDoc as xs:string?, $spaceDoc as xs:string?) as xs:boolean?
{
let $incidentConnections := tokenize($incidentConns, " ")
let $spaceConnections := tokenize($spaceConns, " ")
let $incidentConnTypes := doc($incidentDoc)//connection[@name=$incidentConnections]/type/concat("environment:",data(@name))
let $spaceConnTypes := doc($spaceDoc)//connection[@name=$spaceConnections]/data(@xsi:type)
let $numOfIncidentConns := count($incidentConnTypes)
let $numOfSpaceConns := count($spaceConnTypes)

return 
(: return all space assets that have the same number and types of incident asset connections :)
if ($numOfIncidentConns != 0 and $connectionsKnowledge = "EXACT" and $numOfSpaceConns = $numOfIncidentConns)
then (
let $r:= (
for $iType in $incidentConnTypes
where count(index-of($spaceConnTypes,$iType)) = count(index-of($incidentConnTypes, $iType))
return 1
)
return not(empty($r)) and count($r) = count($incidentConnTypes)
(: return false if exact is set in incident asset for connections and the numbers of connections are different in both :)
) else if ($numOfIncidentConns != 0 and $connectionsKnowledge = "EXACT" and $numOfSpaceConns != $numOfIncidentConns)
then( 
	false()
(: return all space assets that have the types of incident asset connections (space asset can have more connections) :)
) else if ($numOfIncidentConns != 0 and $connectionsKnowledge != "EXACT" and $numOfSpaceConns >= $numOfIncidentConns)
then(
let $r:= (
for $iType in $incidentConnTypes
where count(index-of($spaceConnTypes,$iType)) >= count(index-of($incidentConnTypes, $iType)) 
return 1
)
return not(empty($r)) and count($r) = count($incidentConnTypes)
(: return false if some types in in incident asset connections types are not available in the space asset connections :)
) else if ($numOfIncidentConns != 0 and $connectionsKnowledge != "EXACT" and $numOfSpaceConns lt $numOfIncidentConns)
then (
 false()
(: return all space assets that have the same number and types of incident asset connections which is zero :)
) else if ($numOfIncidentConns = 0 and $connectionsKnowledge = "EXACT")
then(
 $numOfSpaceConns = 0
(: incident asset has no connections and knowledge about them is either partial or unknow thus should not affect selection :)
) else if ($numOfIncidentConns = 0 and $connectionsKnowledge != "EXACT")
then (
true()
) else()
};

(:
:: This function compares the number and types of contained assets (i.e. children) found in an incident asset to that of a space asset
:: Returns true if they match according to some conditions (e.g., Knowledge be "exact"), false otherwise
:)
declare function local:compare-containedAssets($incidentContainedAssets as xs:string?, $childrenKnowledge as xs:string?, 
$spaceContainedAssets as xs:string?, $incidentDoc as xs:string?, $spaceDoc as xs:string?) as xs:boolean?
{
let $incidentChildren := tokenize($incidentContainedAssets, " ")
let $spaceChildren := tokenize($spaceContainedAssets, " ")
let $incidentChildrenTypes := doc($incidentDoc)//element()[@name=$incidentChildren]/type/concat("environment:",data(@name))
let $spaceChildrenTypes := doc($spaceDoc)//element()[@name=$spaceChildren]/data(@xsi:type)
let $numOfIncidentChildren := count($incidentChildrenTypes)
let $numOfSpaceChildren := count($spaceChildrenTypes)

return 
(: return all space assets that have the same number and types of incident asset connections :)
if ($numOfIncidentChildren != 0 and $childrenKnowledge = "EXACT" and $numOfSpaceChildren = $numOfIncidentChildren)
then (
let $r:= (
for $iType in $incidentChildrenTypes
where count(index-of($spaceChildrenTypes,$iType)) = count(index-of($incidentChildrenTypes, $iType))
return 1
)
return not(empty($r)) and count($r) = count($incidentChildrenTypes)
(: return false if exact is set in incident asset for connections and the numbers of connections are different in both :)
) else if ($numOfIncidentChildren != 0 and $childrenKnowledge = "EXACT" and $numOfSpaceChildren != $numOfIncidentChildren)
then( 
	false()
(: return all space assets that have the types of incident asset connections (space asset can have more connections) :)
) else if ($numOfIncidentChildren != 0 and $childrenKnowledge != "EXACT" and $numOfSpaceChildren >= $numOfIncidentChildren)
then(
let $r:= (
for $iType in $incidentChildrenTypes
where count(index-of($spaceChildrenTypes,$iType)) >= count(index-of($incidentChildrenTypes, $iType)) 
return 1
)
return not(empty($r)) and count($r) = count($incidentChildrenTypes)
(: return false if some types in in incident asset connections types are not available in the space asset connections :)
) else if ($numOfIncidentChildren != 0 and $childrenKnowledge != "EXACT" and $numOfSpaceChildren lt $numOfIncidentChildren)
then (
 false()
(: return all space assets that have the same number and types of incident asset connections which is zero :)
) else if ($numOfIncidentChildren = 0 and $childrenKnowledge = "EXACT")
then(
 $numOfSpaceChildren = 0
(: incident asset has no connections and knowledge about them is either partial or unknow thus should not affect selection :)
) else if ($numOfIncidentChildren = 0 and $childrenKnowledge != "EXACT")
then (
true()
) else()
};

(:
:: This function compares properties of an incident asset with properties of a space asset
:: The comparison is based on the name and value be equal in both 
:: Returns true if all properties in the incident asset are found in the space asset [or] if incident asset properties number is zero
:: returns false otherwise
:)
declare function local:compare-properties($incidentProperties as element(property)*, $spaceProperties as element(property)*, 
$incidentDoc as xs:string?, $spaceDoc as xs:string?) as xs:boolean? 
{

let $res:= true()
return
(: if the incident asset has any properties then match to that of space asset else return true (i.e. not considered for matching :)
if (count($incidentProperties[@name!=""])>0)  
then (
let $comp := ( 
for $property in $incidentProperties, $sprop in $spaceProperties
where $property/@name = $sprop/@name and $property/@value = $sprop/@value 
return 1)

return not(empty($comp)) and count($comp) = count($incidentProperties)
)
(: if the incident asset has no properties return true (i.e. not considered in matching :) 
else ( 
$res
)
};

(:
:: This function compares an asset from incident model with an asset from space model based on a specific criteria
:: Criteria: 
::			(1) Type of asset
::			(2) Number and types of asset connections
::			(3) Asset's contained_assets (i.e. children) number and types
::			(4) Type of asset's Parent
::			(5) Status of the asset
::			(6) Properties of the asset 
:: Returns true if all correctly matched
:)
declare function local:compare-assets($incidentAsset as element()?, $spaceAsset as element()?, 
$incidentDoc as xs:string?, $spaceDoc as xs:string?) as xs:boolean?
{
(:match based on type of asset:)
let $match:=(
$spaceAsset/@xsi:type=concat("environment:", $incidentAsset/type/@name) 

(:match based on having the same parent type:)
and (
let $sParent := data($spaceAsset/@parentAsset) 
let $iParent := data($incidentAsset/@parentEntity)
return if (not($iParent)) then true() (:if the incident asset's parent is empty then return true:)
else if ($iParent and not($sParent)) then false() (:if the incident asset's parent has a parent but not the space one:) 
else ( 
doc($spaceDoc)//element()[@name=$sParent]/data(@xsi:type)
= concat("environment:", data(doc($incidentDoc)//element()[@name=$incidentAsset/@parentEntity]/type/@name)))
)

(: match based on number and type of contained assets:)
and local:compare-containedAssets($incidentAsset/@containedEntities, "PAR", $spaceAsset/@containedAssets, $incidentDoc, $spaceDoc)

(:match based on number and type of connections, currently space asset should contain the connections of incident asset:)
and local:compare-connections(concat("",$incidentAsset/data(@connections)), concat("",$incidentAsset/data(@connectionsKnowledge)),
concat("",$spaceAsset/data(@connections)), $incidentDoc, $spaceDoc) 

(: match based on status if incident asset has one:)
and ( let $st := concat("",$incidentAsset/data(@status)) return if($st) then $st = concat("",$spaceAsset/data(@status)) else true())

(: match properties :)
and local:compare-properties($incidentAsset/property, $spaceAsset/property, $incidentDoc, $spaceDoc)
)

return $match
};

(:
:: This function finds all space assets that match the incident asset according to the criteria described in function local:compare-assets
:: Returns space assets matched
:)
declare function local:find-matches($incidentAsset as element()?, 
$incidentDoc as xs:string?, $spaceDoc as xs:string?) as element()*
{
for $spaceAsset in doc($spaceDoc)//asset
where local:compare-assets($incidentAsset, $spaceAsset, $incidentDoc, $spaceDoc) = true()

return $spaceAsset

};


let $incidentDoc := "etc/example/interruption_incident-pattern.cpi"
let $spaceDoc := "etc/example/research_centre_model.environment"
let $incidentAssets := doc($incidentDoc)//cyberPhysical_Incident:IncidentDiagram/(asset, actor, resource)
let $matches := ( 
<matches>{
for $incidentAsset in $incidentAssets
let $mt := local:find-matches($incidentAsset, $incidentDoc, $spaceDoc)/data(@name)
return (
<incidentAsset name="{data($incidentAsset/@name)}">
<matchesNumber>{count($mt)}</matchesNumber>
{
for $m in $mt
return 
<match>{$m}</match>}
</incidentAsset>

)
}</matches>
)

let $str := (
for $i in $matches/incidentAsset
return concat(data($i/@name),":", string-join($i/match/text(),"-"))
)
(:
let $seq := (
<sequence>{
for $inc in $matches/incidentAsset, $mat in $inc/data(match)
return (
 for $i in $matches/incidentAsset[data(@name) != $inc/data(@name)], $j in $i/match[text() != $mat]
return <incidentAsset name="{data($inc/@name)}" matchName="{data($mat)}">
<asset name="{data($i/@name)}" match="{data($j/text())}" />
</incidentAsset>
)
}</sequence>
)
:)
(: Result shown in XML :)

return 

<r>{$str}</r>



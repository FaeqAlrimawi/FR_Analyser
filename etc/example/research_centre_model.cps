<?xml version="1.0" encoding="UTF-8"?>
<environment:EnvironmentDiagram xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:environment="http://www.example.org/environment">
  <asset xsi:type="environment:Building" name="Research_Centre" description="" control="Building" containedAssets="second_floor">
    <type name="building"/>
  </asset>
  <asset xsi:type="environment:Floor" name="second_floor" description="" control="Floor" containedAssets="Control_room hallway Server_room Toilet"/>
  <asset xsi:type="environment:Room" connections="ToiletRm-Hallway" name="Toilet" control="Room" containedAssets="SL1" parentAsset="second_floor"/>
  <asset xsi:type="environment:Room" connections="CntrlRm-Hallway" name="Control_room" control="Room" containedAssets="SL2 Workstation1" parentAsset="second_floor"/>
  <asset xsi:type="environment:Room" connections="ServerRm-Hallway" name="Server_room" control="Room" containedAssets="SL3 FireAlarm1 AirConditioning" parentAsset="second_floor"/>
  <asset xsi:type="environment:SmartLight" connections="SL1-DN" name="SL1" control="SmartLight" parentAsset="Toilet"/>
  <asset xsi:type="environment:SmartLight" connections="SL2-DN" name="SL2" control="SmartLight" parentAsset="Control_room"/>
  <asset xsi:type="environment:SmartLight" connections="SL3-DN" name="SL3" control="SmartLight" parentAsset="Server_room"/>
  <asset xsi:type="environment:HVAC" connections="HVAC-DN" name="AirConditioning" control="HVAC" parentAsset="Server_room"/>
  <asset xsi:type="environment:FireAlarm" connections="FA-DN" name="FireAlarm1" control="FireAlarm" parentAsset="Server_room"/>
  <asset xsi:type="environment:Hallway" connections="CntrlRm-Hallway ServerRm-Hallway ToiletRm-Hallway" name="hallway" control="Hallway" containedAssets="Visitor1" parentAsset="second_floor"/>
  <asset xsi:type="environment:Server" connections="Server-DN" name="Server1" control="Server" parentAsset="Server_room"/>
  <asset xsi:type="environment:Workstation" connections="Workstation-DN" name="Workstation1" control="Workstation" parentAsset="Control_room"/>
  <asset xsi:type="environment:DigitalAsset" connections="FA-DN HVAC-DN Server-DN SL1-DN SL2-DN SL3-DN" name="busNetwork" description="" control="InstallationBus"/>
  <asset xsi:type="environment:Actor" name="Visitor1" control="Visitor" containedAssets="Laptop1" parentAsset="hallway"/>
  <asset xsi:type="environment:Laptop" name="Laptop1" control="Laptop" containedAssets="SoftwareX" parentAsset="Visitor1"/>
  <asset xsi:type="environment:Application" name="SoftwareX" control="Software" parentAsset="Laptop1"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="FireAlarm1" name="FA-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="AirConditioning" name="HVAC-DN" type="DigitalConnection"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="Server1" name="Server-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL1" name="SL1-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL2" name="SL2-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL3" name="SL3-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="Workstation1" name="Workstation-DN"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="Control_room" asset2="hallway" name="CntrlRm-Hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="Toilet" asset2="hallway" name="ToiletRm-Hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="Server_room" asset2="hallway" name="ServerRm-Hallway"/>
</environment:EnvironmentDiagram>

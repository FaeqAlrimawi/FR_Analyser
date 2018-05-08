<?xml version="1.0" encoding="UTF-8"?>
<environment:EnvironmentDiagram xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:environment="http://www.example.org/environment">
  <asset xsi:type="environment:Building" name="Research_Centre" description="" control="Building" containedAssets="fourteenth_floor">
    <type name="building"/>
  </asset>
  <asset xsi:type="environment:Floor" name="fourteenth_floor" description="" control="Floor"/>
  <asset xsi:type="environment:SmartLight" connections="SL1-DN" name="SL1" control="SmartLight"/>
  <asset xsi:type="environment:SmartLight" connections="SL2-DN" name="SL2" control="SmartLight"/>
  <asset xsi:type="environment:SmartLight" connections="SL3-DN" name="SL3" control="SmartLight"/>
  <asset xsi:type="environment:HVAC" connections="HVAC-DN" name="AirConditioning" control="HVAC"/>
  <asset xsi:type="environment:FireAlarm" connections="FA-DN" name="FireAlarm1" control="FireAlarm"/>
  <asset xsi:type="environment:Server" connections="Server-DN" name="Server1" control="Server"/>
  <asset xsi:type="environment:Workstation" connections="Workstation-DN" name="Workstation1" control="Workstation"/>
  <asset xsi:type="environment:DigitalAsset" connections="FA-DN HVAC-DN Server-DN SL1-DN SL2-DN SL3-DN" name="busNetwork" description="" control="InstallationBus"/>
  <asset xsi:type="environment:Actor" name="Visitor1" control="Visitor" containedAssets="Laptop1"/>
  <asset xsi:type="environment:Laptop" name="Laptop1" control="Laptop" containedAssets="SoftwareX" parentAsset="Visitor1"/>
  <asset xsi:type="environment:Application" name="SoftwareX" control="Software" parentAsset="Laptop1"/>
  <asset xsi:type="environment:Lab" name="instructorsLab1" containedAssets="desktop1_1 desktop1_2 desktop1_3"/>
  <asset xsi:type="environment:Desktop" connections="d1_1" name="desktop1_1" description="" control="Desktop" parentAsset="instructorsLab1"/>
  <asset xsi:type="environment:Desktop" connections="d1_2" name="desktop1_2" control="Desktop" parentAsset="instructorsLab1"/>
  <asset xsi:type="environment:Desktop" name="desktop1_3" control="Desktop" parentAsset="instructorsLab1"/>
  <asset xsi:type="environment:Desktop" connections="d2_1" name="desktop2_1" control="Desktop" parentAsset="instructorsLab2"/>
  <asset xsi:type="environment:Desktop" name="desktop2_2" control="Desktop" parentAsset="instructorsLab2"/>
  <asset xsi:type="environment:Desktop" connections="d3_1" name="desktop3_1" control="Desktop" parentAsset="instructorsLab3" model=""/>
  <asset xsi:type="environment:Desktop" name="desktop3_2" description="" control="Desktop" parentAsset="instructorsLab3"/>
  <asset xsi:type="environment:Desktop" connections="d4_1" name="desktop4_1" control="Desktop" parentAsset="instructorsLab4" model=""/>
  <asset xsi:type="environment:Desktop" name="desktop4_2" control="Desktop" parentAsset="instructorsLab4" model=""/>
  <asset xsi:type="environment:Desktop" connections="d5_1" name="desktop5_1" control="Desktop" parentAsset="instructorsLab5"/>
  <asset xsi:type="environment:Desktop" connections="d5_2" name="desktop5_2" control="Desktop" parentAsset="instructorsLab5"/>
  <asset xsi:type="environment:Desktop" connections="d6_1" name="desktop6_1" control="Desktop" parentAsset="instructorsLab6"/>
  <asset xsi:type="environment:Desktop" connections="d6_2" name="desktop6_2" control="Desktop" parentAsset="instructorsLab6"/>
  <asset xsi:type="environment:Desktop" connections="d7_1" name="desktop7_1" control="Desktop" parentAsset="instructorsLab7"/>
  <asset xsi:type="environment:Desktop" name="desktop7_2" control="Desktop" parentAsset="instructorsLab7"/>
  <asset xsi:type="environment:IPNetwork" connections="d1_1 d1_2 d2_1 d3_1 d4_1 d5_1 d5_2 d6_1 d6_2 d7_1 d8_2" name="IPnetwork1" control="IPNetwork" Protocol="TCP/IP" encryption="MACsec">
    <type name="Ethernet"/>
  </asset>
  <asset xsi:type="environment:Lab" name="instructorsLab2" control="Lab" containedAssets="desktop2_1 desktop2_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="instructorsLab3" description="" control="Lab" containedAssets="desktop3_1 desktop3_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="instructorsLab4" control="Lab" containedAssets="desktop4_1 desktop4_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="instructorsLab5" control="Lab" containedAssets="desktop5_1 desktop5_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="instructorsLab6" control="Lab" containedAssets="desktop6_1 desktop6_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="instructorsLab7" control="Lab" containedAssets="desktop7_1 desktop7_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="openLab_hallway1 openLab_hallway2" name="openLab" control="Lab_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="empiricalLab_hallway1 empiricalLab_hallway2 empiricalLab_hallway3 empiricalLab_hallway4" name="empiricalLab" control="Lab_4" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" connections="informaticsLab_hallway1 informaticsLab_hallway2 informaticsLab_hallway3 informaticsLab_hallway4" name="informaticsLab" control="Lab_4" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Toilet" connections="mensToilet_hallway" name="mensToilet" control="Toilet" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Toilet" connections="womensToilet_hallway" name="women'sToilet" control="Toilet" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Toilet" connections="disabledToilet_hallway" name="disabledToilet" control="Toilet" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Kitchen" connections="kitchen_hallway kitchen_airConditioningRoom" name="kitchen" control="Kitchen" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Room" connections="kitchen_airConditioningRoom" name="airConditioningRoom" control="Room" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="researchLab1" description="" control="Lab" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="researchLab2" control="Lab" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="visitingLab1" control="Lab" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="meetingRoom" control="Room" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Stairs" connections="roomA_stairsA" name="stairsA" description=""/>
  <asset xsi:type="environment:Stairs" connections="stairsB_hallway" name="stairsB" description=""/>
  <asset xsi:type="environment:Lounge" connections="lounge_hallway1 lounge_hallway2" name="lounge1" control="Lounge"/>
  <asset xsi:type="environment:ElevatorsArea" name="elevatorsArea" control="ElevatorsArea" containedAssets="elevator1 elevator2 elevator3 elevator4 elevator5 elevator6"/>
  <asset xsi:type="environment:Elevator" name="elevator1" control="Elevator" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" name="elevator2" control="Elevator" parentAsset="elevatorsArea" model=""/>
  <asset xsi:type="environment:Elevator" name="elevator3" control="Elevator" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" name="elevator4" control="Elevator" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" name="elevator5" control="Elevator" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" name="elevator6" control="Elevator" parentAsset="elevatorsArea"/>
  <asset xsi:type="environment:Elevator" connections="roomA_emrgElv" name="emergencyElevator" control="Elevator"/>
  <asset xsi:type="environment:Room" connections="roomA_emrgElv roomA_stairsA roomA_hallway" name="roomA" control="Room" containedAssets="cardReader1"/>
  <asset xsi:type="environment:CardReader" name="cardReader1" control="CardReader" parentAsset="roomA"/>
  <asset xsi:type="environment:Hallway" connections="empiricalLab_hallway1 informaticsLab_hallway1 instructorsLab1_hallway instructorsLab2_hallway instructorsLab3_hallway instructorsLab4_hallway instructorsLab5_hallway instructorsLab6_hallway instructorsLab7_hallway openLab_hallway1 researchLab1_hallway researchLab2_hallway visitingLab1_hallway visitingLab2_hallway elevatorsArea_hallway stairsB_hallway roomA_hallway empiricalLab_hallway2 empiricalLab_hallway3 informaticsLab_hallway2 openLab_hallway2 informaticsLab_hallway3 informaticsLab_hallway4 empiricalLab_hallway4 disabledToilet_hallway kitchen_hallway mensToilet_hallway womensToilet_hallway lounge_hallway1 lounge_hallway2" name="hallway" control="Hallway" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Lab" name="instructorsLab8" control="Lab" containedAssets="desktop8_1 desktop8_2" parentAsset="fourteenth_floor"/>
  <asset xsi:type="environment:Desktop" connections="d7_1" name="desktop8_1" control="Desktop" parentAsset="instructorsLab8"/>
  <asset xsi:type="environment:Desktop" connections="d8_2" name="desktop8_2" control="Desktop" parentAsset="instructorsLab8"/>
  <asset xsi:type="environment:Server" name="server1" control="Server" parentAsset="empiricalLab"/>
  <asset xsi:type="environment:Server" name="server2" control="Server" parentAsset="informaticsLab"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="FireAlarm1" name="FA-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="AirConditioning" name="HVAC-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="Server1" name="Server-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL1" name="SL1-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL2" name="SL2-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="SL3" name="SL3-DN"/>
  <connection xsi:type="environment:DigitalConnection" asset1="busNetwork" asset2="Workstation1" name="Workstation-DN"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop1_1" name="d1_1" type="" protocol="TCP/IP"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop1_2" name="d1_2"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop2_1" name="d2_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop3_1" name="d3_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop4_1" name="d4_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop5_1" name="d5_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop5_2" name="d5_2"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop6_1" name="d6_1"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop6_2" name="d6_2"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop7_1" name="d7_1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="emergencyElevator" asset2="roomA" name="roomA_emrgElv"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="stairsA" asset2="roomA" name="roomA_stairsA"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="empiricalLab" name="empiricalLab_hallway1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="informaticsLab" name="informaticsLab_hallway1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab1" name="instructorsLab1_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab2" name="instructorsLab2_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab3" name="instructorsLab3_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab4" name="instructorsLab4_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab5" name="instructorsLab5_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab6" name="instructorsLab6_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="instructorsLab7" name="instructorsLab7_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="openLab" name="openLab_hallway1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="researchLab1" name="researchLab1_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="researchLab2" name="researchLab2_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="visitingLab1" name="visitingLab1_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="meetingRoom" name="visitingLab2_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="elevatorsArea" name="elevatorsArea_hallway">
    <constraints>Open:WorkingHours[8:30-19]</constraints>
    <constraints>CardOnly:WorkingHours[19-22]</constraints>
    <constraints>Closed:OffWorkingHours[22-8:30]</constraints>
  </connection>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="stairsB" name="stairsB_hallway">
    <constraints>Open:WorkingHours[8:30-19]</constraints>
    <constraints>Closed:OffWorkingHours[19-8:30]</constraints>
  </connection>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="roomA" name="roomA_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="informaticsLab" name="informaticsLab_hallway2"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="openLab" name="openLab_hallway2"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="empiricalLab" name="empiricalLab_hallway2"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="empiricalLab" name="empiricalLab_hallway3"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="empiricalLab" name="empiricalLab_hallway4"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="informaticsLab" name="informaticsLab_hallway3"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="informaticsLab" name="informaticsLab_hallway4"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="disabledToilet" name="disabledToilet_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="mensToilet" name="mensToilet_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="women'sToilet" name="womensToilet_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="kitchen" name="kitchen_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="airConditioningRoom" asset2="kitchen" name="kitchen_airConditioningRoom"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="kitchen" name="kitchen_hallway"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="lounge1" name="lounge_hallway1"/>
  <connection xsi:type="environment:PhysicalConnection" asset1="hallway" asset2="lounge1" name="lounge_hallway2"/>
  <connection xsi:type="environment:IPConnection" asset1="IPnetwork1" asset2="desktop8_2" name="d8_2"/>
</environment:EnvironmentDiagram>

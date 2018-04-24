package ie.lero.spare.pattern_extraction;

import environment.Asset;
import environment.Building;
import environment.ComputingDevice;
import environment.DigitalAsset;
import environment.EnvironmentDiagram;
import environment.Floor;
import environment.HVAC;
import environment.Kitchen;
import environment.Lab;
import environment.PhysicalAsset;
import environment.Room;
import environment.Server;
import environment.SmartLight;
import environment.Status;
import environment.smartbuildingFactory;
import environment.impl.HVACImpl;

public class SystemMetaModelLevels {
	
	public  static void main(String [] args) {
		
		//ExtendedMetaData modelMetaData = new BasicExtendedMetaData(myResourceSet.getPackageRegistry());
		smartbuildingFactory instance = smartbuildingFactory.eINSTANCE;
		HVAC s = instance.createHVAC();
		s.setStatus(Status.OFF);
		//System.out.println(s.getStatus()+" "+s.isAbstractable());
		Floor f = instance.createFloor();
		Room r1 = instance.createRoom();
		Kitchen kit = instance.createKitchen();
		Lab lb = instance.createLab();
		SmartLight sl = instance.createSmartLight();
		SmartLight sl2 = instance.createSmartLight();
		Asset abstractedAsset;
		ComputingDevice dev = instance.createComputingDevice();
		DigitalAsset dig = instance.createDigitalAsset();
		PhysicalAsset phys = instance.createPhysicalAsset();
		Server ser = instance.createServer();
		
		//Asset fAbstracted = f.abstractAsset();
		//Asset slAbstracted = sl.abstractAsset();
		//abstractedAsset = sl.abstractAsset();
		
		lb.isSimilarTo(dev);
		//Room res =f.abstract_();//r1.abstract_();//f.abstract_();
		
		/*if(res != null) {
			System.out.println(res.getName());
		} else {
			System.out.println("room is null");
		}*/
		
	/*	if(abstractedAsset != null) {
		System.out.println(abstractedAsset.getClass());
	} else {
		System.out.println("abstracted asset is null");
	}*/
		
		
	}

}

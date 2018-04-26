package ie.lero.spare.pattern_extraction;

import org.eclipse.emf.common.util.EList;

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
		Lab lb2 = instance.createLab();
		SmartLight sl = instance.createSmartLight();
		SmartLight sl2 = instance.createSmartLight();
		SmartLight sl3 = instance.createSmartLight();
		SmartLight sl4 = instance.createSmartLight();
		SmartLight sl5 = instance.createSmartLight();
		SmartLight sl6 = instance.createSmartLight();
		SmartLight sl7 = instance.createSmartLight();
		Asset abstractedAsset;
		ComputingDevice dev = instance.createComputingDevice();
		DigitalAsset dig = instance.createDigitalAsset();
		PhysicalAsset phys = instance.createPhysicalAsset();
		Server ser = instance.createServer();
		
		//Asset fAbstracted = f.abstractAsset();
		//Asset slAbstracted = sl.abstractAsset();
		//abstractedAsset = sl.abstractAsset();
		
		System.out.println("Max. similarity value: " + Asset.SIMILARITY_MAXIMUM_VALUE);
		System.out.println("Threshold similarity value: " + Asset.SIMILARITY_THRESHOLD);
		kit.setParentAsset(f);
		lb.setParentAsset(f);
		lb2.setParentAsset(f);
		kit.getContainedAssets().add(sl);
		//kit.getContainedAssets().add(sl6);
		kit.getContainedAssets().add(sl7);
		kit.getContainedAssets().add(sl2);
		kit.getContainedAssets().add(sl3);
		lb.getContainedAssets().add(sl4);
		lb2.getContainedAssets().add(sl6);
		//lb.getContainedAssets().add(sl5);
		System.out.println(lb.isSimilarTo(lb2));
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

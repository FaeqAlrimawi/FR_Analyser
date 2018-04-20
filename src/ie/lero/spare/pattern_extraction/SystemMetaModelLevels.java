package ie.lero.spare.pattern_extraction;

import environment.Building;
import environment.EnvironmentDiagram;
import environment.HVAC;
import environment.Status;
import environment.smartbuildingFactory;
import environment.impl.HVACImpl;

public class SystemMetaModelLevels {
	
	public  static void main(String [] args) {
		
		//ExtendedMetaData modelMetaData = new BasicExtendedMetaData(myResourceSet.getPackageRegistry());
		HVAC s = smartbuildingFactory.eINSTANCE.createHVAC();
		s.setStatus(Status.OFF);
		System.out.println(s.getStatus()+" "+s.isAbstractable());
		
		
		
	}

}

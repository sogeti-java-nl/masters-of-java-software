package nl.sogeti.jcn.moj.client.eclipse.wizard;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;

public class ConnectorWizard extends Wizard {
	@Override
	public void addPages() {
		WizardPage loginPage = new LoginPage("LoginPage");
		addPage(loginPage);
		
	}
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}

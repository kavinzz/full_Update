import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;


public class MainWindow {
	
	private static Label stateLable;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		Shell shlZhzxserver = new Shell();
		shlZhzxserver.setSize(449, 89);
		shlZhzxserver.setText("ZHZX_Server");
		
		stateLable = new Label(shlZhzxserver, SWT.NONE);
		stateLable.setFont(SWTResourceManager.getFont("·ÂËÎ", 16, SWT.BOLD));
		stateLable.setBounds(10, 10, 413, 30);

		shlZhzxserver.open();
		shlZhzxserver.layout();
		while (!shlZhzxserver.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private void setLableText(final String str){
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				stateLable.setText(str);
			}
		});
	}
}

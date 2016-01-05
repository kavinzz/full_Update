import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.ProgressBar;


public class Main {

	protected Shell ctsUpdateShell;
	private Label stateTab;
	private ProgressBar progressBar;
	private String netVer = null;
	private static String localCfgFile = "D:\\CTS\\source\\config.properties";
	private static String SERVER_IP = "172.16.0.216";
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Main window = new Main();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		ctsUpdateShell.open();
		ctsUpdateShell.layout();
		while (!ctsUpdateShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		ctsUpdateShell = new Shell(SWT.BORDER | SWT.TITLE);
		ctsUpdateShell.setSize(332, 114);
		ctsUpdateShell.setText("\u6307\u6325\u4E2D\u5FC3\u7EFC\u5408\u4E1A\u52A1\u7BA1\u7406\u7CFB\u7EDF\u66F4\u65B0\u7A0B\u5E8F");
		ctsUpdateShell.setLocation(Display.getCurrent().getClientArea().width /2 - ctsUpdateShell.getShell().getSize().x / 2,
				Display.getCurrent().getClientArea().height /2 - ctsUpdateShell.getShell().getSize().y/2);
		
		stateTab = new Label(ctsUpdateShell, SWT.NONE);
		stateTab.setAlignment(SWT.CENTER);
		stateTab.setFont(SWTResourceManager.getFont("宋体", 12, SWT.NORMAL));
		stateTab.setBounds(82, 10, 136, 21);
		stateTab.setText("\u6B63\u5728\u68C0\u67E5\u66F4\u65B0...");
		
		progressBar = new ProgressBar(ctsUpdateShell, SWT.NONE);
		progressBar.setFont(SWTResourceManager.getFont("宋体", 10, SWT.NORMAL));
		progressBar.setBounds(10, 38, 294, 27);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		progressBar.setVisible(false);
		
		new CheckUpdate().start();
	}
	
	class CheckUpdate extends Thread{
		private boolean isUpdate = false;
		
		
		private String localVer = null;
		private String netFilePath = null;
		private String netURL = null;
		private String localFilePath = "D:\\CTS\\client.jar";
		private String tempFilePath = "D:\\CTS\\temp.jar";
		
		public CheckUpdate(){
			
			try {
				Properties props = new Properties();
				InputStream in;
				in = new FileInputStream("D:\\CTS\\source\\config.properties");
				props.load(in);
				SERVER_IP = props.getProperty("ip");
				netURL = "http://"+SERVER_IP+"/client/version.txt";
				netFilePath = "http://"+SERVER_IP+"/client/client.jar";
			} catch (IOException e) {
				showMsgBox(e.getMessage());
			}
		}
		
		public void run(){
			BufferedReader br = null;
			InputStreamReader isr = null;
			InputStream is = null;
			try {
				final URL url = new URL(netURL);
				is = url.openStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);
				netVer = br.readLine();
				
				Properties props = new Properties();
				InputStreamReader in = new InputStreamReader(new FileInputStream(localCfgFile),"UTF-8");
				props.load(in);
				localVer = props.getProperty("version");
				
				if(netVer.equals(localVer)){
					setStateTex("版本已是最新！");
					isUpdate = false;
				}else{
					setStateTex("检测到新版本。");
					isUpdate = true;
				}
				
			} catch (IOException e) {
				showMsgBox(e.getMessage());
			}finally{
				try {
					if(br != null)br.close();
					if(isr != null)isr.close();
					if(is !=null) is.close();
				} catch (IOException e) {
					showMsgBox(e.getMessage());
				}
				
			}
			
			if(isUpdate){
				File localFile = new File(localFilePath);
				File tempFile = new File(tempFilePath);
				FileOutputStream fos = null;
				BufferedInputStream bis = null;
				HttpURLConnection httpConn = null;
				
				try {
					URL url = new URL(netFilePath);
					httpConn = (HttpURLConnection) url.openConnection();
					httpConn.connect();
					
					is = httpConn.getInputStream();
					bis = new BufferedInputStream(is);
					fos = new FileOutputStream(tempFile);
					
					byte[] buffer = new byte[bis.available()];
					int size = 0;
					setStateTex("正在下载新版本...");
					while ((size = bis.read(buffer)) != -1) {
						fos.write(buffer,0,size);
						fos.flush();	
						setProgressBar();
					}
					setStateTex("下载完成...");
					copyFile(localFile, tempFile);
					
				} catch (IOException e) {
					showMsgBox(e.getMessage());
				}finally{
					try {
						fos.close();
						bis.close();
						is.close();
						httpConn.disconnect();
					} catch (IOException e) {
						showMsgBox(e.getMessage());
					}
					
				}
			}
			setStateTex("启动应用程序...");
			try {
				Thread.sleep(500);
				Runtime.getRuntime().exec("cmd /c java -jar " + localFilePath);
			} catch (IOException | InterruptedException e) {
				showMsgBox(e.getMessage());
			}
			File tempFile = new File(tempFilePath);
			if(tempFile.exists()) tempFile.delete();
			System.exit(0);
		}
	}
	
	private void copyFile(File oFile,File nFile){
		setStateTex("正在更新文件");

		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		try {
			if(oFile.exists()) oFile.delete();
			
			fis = new FileInputStream(nFile);
			fos = new FileOutputStream(oFile); 
			
			byte[] buffer = new byte[fis.available()];
			int size = 0;
			while ((size = fis.read(buffer))!=-1) {
				fos.write(buffer,0,size);
				fos.flush();
				setProgressBar();
			}
			
			setStateTex("文件更新完成！");
			updateLocalVersion();
			
		} catch (IOException e) {
			showMsgBox(e.getMessage());
		}finally{
			try {
				fos.close();
				fis.close();
			} catch (IOException e) {
				showMsgBox(e.getMessage());
			}
		}
	}
	
	private void updateLocalVersion(){
		
		try {
			Properties props = new Properties();
			InputStreamReader in = new InputStreamReader(new FileInputStream(localCfgFile),"UTF-8");
			props.load(in);
			
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(localCfgFile),"UTF-8");
			props.put("version", netVer);
			props.store(out, "update");
		} catch (IOException e) {
			showMsgBox(e.getMessage());
		}
		
	}
	
	private void setStateTex(final String msg){
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				stateTab.setText(msg);
			}
		});
	}
	
	private void setProgressBar(){
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				progressBar.setVisible(true);
				progressBar.setSelection(progressBar.getSelection() + 1);
				
			}
		});
	}
	
	private void showMsgBox(final String msg){
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				MessageBox msgBox = new MessageBox(ctsUpdateShell,SWT.ERROR|SWT.ICON_ERROR|SWT.OK);
				msgBox.setText("错误");
				msgBox.setMessage(msg);
				msgBox.open();
				if(msgBox.open() == SWT.OK){
					ctsUpdateShell.dispose();
					System.exit(0);
				}
			}
		});
	}
}

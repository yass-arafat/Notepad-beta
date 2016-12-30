import java.io.*;  
import java.util.Date;  
import java.awt.*;  
import java.awt.event.*;  

import javax.swing.*;  
import javax.swing.event.*;   

//------------------------------------------File Operation starts from here-----------------------------------------//
class FileOperation{
	Notepad_beta npd;

	boolean saved;
	boolean newFileFlag;
	
	String fileName;
	String applicationTitle="Javapad";

	File fileRef;
	JFileChooser chooser;

//-----------------------------------------------Functions-----------------------------------------------------------//
	boolean isSave(){
		
		return saved;
		
	}
	
	void setSave(boolean saved){
		
		this.saved=saved;
		
	}
	
	String getFileName(){
		
		return new String(fileName);
		
	}
	
	void setFileName(String fileName){
		
		this.fileName=new String(fileName);
		
	}
	
//------------------------------------------File OPeration Constructor-------------------------------------------//
	
	FileOperation(Notepad_beta npd)
	{
		this.npd=npd;

		saved=true;
		newFileFlag=true;
		fileName=new String("Untitled");
		fileRef=new File(fileName);
		this.npd.f.setTitle(fileName+" - "+applicationTitle);

		chooser=new JFileChooser();
		chooser.setCurrentDirectory(new File("."));

	}
	
//----------------------------Functions Operation----------------------------------------------------------------//

	boolean saveFile(File temp)
	{
		FileWriter fout=null;
		try
		{
			fout=new FileWriter(temp);
			fout.write(npd.textArea.getText());
		}
		catch(IOException ioe){updateStatus(temp,false);return false;}
		finally
		{
			try{
				fout.close();
				}
			catch(IOException excp){}
		}
		
		updateStatus(temp,true);
		return true;
	}
	
//----------------------------Save This File-------------------------------------------------------------------------//

	boolean saveThisFile()
	{

		if(!newFileFlag)
		{
			return saveFile(fileRef);
		}

		return saveAsFile();
	}
	
//--------------------------Save As File---------------------------------------------------------------------------//
	
	boolean saveAsFile()
	{
		File temp=null;
		chooser.setDialogTitle("Save As...");
		chooser.setApproveButtonText("Save Now"); 
		chooser.setApproveButtonMnemonic(KeyEvent.VK_S);
		chooser.setApproveButtonToolTipText("Click me to save!");

		do
		{
			if(chooser.showSaveDialog(this.npd.f)!=JFileChooser.APPROVE_OPTION)
				return false;
			
			temp=chooser.getSelectedFile();
			
			if(!temp.exists()) break;
			
			if(JOptionPane.showConfirmDialog(
					this.npd.f,"<html>"+temp.getPath()+" already exists.<br>Do you want to replace it?<html>",
					"Save As",JOptionPane.YES_NO_OPTION
				)==JOptionPane.YES_OPTION)
				break;
		}while(true);

		return saveFile(temp);
	}

//------------------------------Open File--------------------------------------------------------------------------//

	boolean openFile(File temp)
	{
		FileInputStream fin=null;
		BufferedReader din=null;

		try
		{
			fin=new FileInputStream(temp);
			din=new BufferedReader(new InputStreamReader(fin));
			String str=" ";
			while(str!=null)
			{
				str=din.readLine();
				if(str==null)
					break;
				this.npd.textArea.append(str+"\n");
			}
			
		}
		catch(IOException ioe){updateStatus(temp,false);return false;}
		finally
		{
			try{
				
				din.close();fin.close();
				
			}catch(IOException excp){}
		}
			updateStatus(temp,true);
			this.npd.textArea.setCaretPosition(0);
			return true;
	}
	
		
	void openFile()
	{
		if(!confirmSave()) return;
		chooser.setDialogTitle("Open File...");
		chooser.setApproveButtonText("Open this"); 
		chooser.setApproveButtonMnemonic(KeyEvent.VK_O);
		chooser.setApproveButtonToolTipText("Click me to open the selected file.!");
		
		File temp=null;
		do
		{
			if(chooser.showOpenDialog(this.npd.f)!=JFileChooser.APPROVE_OPTION)
				return;
			temp=chooser.getSelectedFile();
			
			if(temp.exists())	break;
			
			JOptionPane.showMessageDialog(this.npd.f,
					"<html>"+temp.getName()+"<br>file not found.<br>"+
							"Please verify the correct file name was given.<html>",
							"Open",	JOptionPane.INFORMATION_MESSAGE);
			
		} while(true);
		
		this.npd.textArea.setText("");
		
		if(!openFile(temp))
		{
			fileName="Untitled"; saved=true; 
			this.npd.f.setTitle(fileName+" - "+applicationTitle);
		}
		if(!temp.canWrite())
			newFileFlag=true;
		
	}
	
//-----------------------------------------Update Status------------------------------------------------------------//
	void updateStatus(File temp,boolean saved)
	{
		if(saved)
		{
			this.saved=true;
			fileName=new String(temp.getName());
			if(!temp.canWrite())
			{
				fileName+="(Read only)"; newFileFlag=true;
			}
			fileRef=temp;
			npd.f.setTitle(fileName + " - "+applicationTitle);
			npd.statusBar.setText("File : "+temp.getPath()+" saved/opened successfully.");
			newFileFlag=false;
		}
		else
		{
			npd.statusBar.setText("Failed to save/open : "+temp.getPath());
		}
	}

//---------------------------------Confirm Save-------------------------------------------------------------------//
	
	boolean confirmSave()
	{
		String strMsg="<html>The text in the "+fileName+" file has been changed.<br>"+
				"Do you want to save the changes?<html>";
		if(!saved)
		{
			int x=JOptionPane.showConfirmDialog(this.npd.f,strMsg,applicationTitle,JOptionPane.YES_NO_CANCEL_OPTION);
			
			if(x==JOptionPane.CANCEL_OPTION) return false;
			if(x==JOptionPane.YES_OPTION && !saveAsFile()) return false;
		}
		return true;
	}
	
//------------------------------new File-----------------------------------------------------------------------------//
	
	void newFile()
	{
		if(!confirmSave()) return;
		
		this.npd.textArea.setText("");
		fileName=new String("Untitled");
		fileRef=new File(fileName);
		saved=true;
		newFileFlag=true;
		this.npd.f.setTitle(fileName+" - "+applicationTitle);
	}
	
}

//------------------------------------ end  of class FileOperation------------------------------------------------------//




public class Notepad_beta implements ActionListener, AllMenus{	
	


	JFrame f;
	JTextArea textArea;
	JLabel statusBar;
	

	private String fileName="Untitled";
	private boolean saved=true;
	String applicationName="Javapad";
	

	FileOperation fileHandler;
	FindDialog findReplaceDialog=null;
	JMenuItem cutItem,copyItem, deleteItem, findItem, findNextItem, replaceItem, gotoItem, selectAllItem;

//------------------------------------------Notepad constructor--------------------------------------------	
	Notepad_beta()
	{

		f=new JFrame(fileName+" - "+applicationName);
		textArea=new JTextArea(30,60);
		statusBar=new JLabel("||       Ln 1, Col 1  ",JLabel.RIGHT);
		
		f.add(new JScrollPane(textArea),BorderLayout.CENTER);
		f.add(statusBar,BorderLayout.SOUTH);
		f.add(new JLabel("  "),BorderLayout.EAST);
		f.add(new JLabel("  "),BorderLayout.WEST);
		
			cMenuBar(f);
		System.out.println("Added from Remote");
		f.pack();
		f.setLocation(100,50);
		f.setVisible(true);
		f.setLocation(150,50);
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		fileHandler=new FileOperation(this);
		
	}
//--------------------------------------------End of Notepad constructor--------------------------------------------------------------------//		
		
//--------------------------------------------Creating Menu Bar-----------------------------------------------------------------------------//	
		
		void cMenuBar(JFrame f)
		{
			JMenuBar mb = new JMenuBar();
			JMenuItem temp;  
			
			JMenu fileMenu = createMenu(fileText,KeyEvent.VK_F,mb);
			JMenu editMenu=createMenu(editText,KeyEvent.VK_E,mb);
			
			createMenuItem(fileNew,KeyEvent.VK_N,fileMenu,KeyEvent.VK_N,this);  
			createMenuItem(fileOpen,KeyEvent.VK_O,fileMenu,KeyEvent.VK_O,this);  
			createMenuItem(fileSave,KeyEvent.VK_S,fileMenu,KeyEvent.VK_S,this);  
			createMenuItem(fileSaveAs,KeyEvent.VK_A,fileMenu,KeyEvent.VK_A,this); 
			
			fileMenu.addSeparator(); 
			createMenuItem(fileExit,KeyEvent.VK_X,fileMenu,KeyEvent.VK_X,this); 
			

			temp=createMenuItem(editUndo,KeyEvent.VK_U,editMenu,KeyEvent.VK_Z,this);  
			temp.setEnabled(false);  
			editMenu.addSeparator();  
			
			cutItem=createMenuItem(editCut,KeyEvent.VK_T,editMenu,KeyEvent.VK_X,this);  
			copyItem=createMenuItem(editCopy,KeyEvent.VK_C,editMenu,KeyEvent.VK_C,this);  
			
			createMenuItem(editPaste,KeyEvent.VK_P,editMenu,KeyEvent.VK_V,this);  
			deleteItem=createMenuItem(editDelete,KeyEvent.VK_L,editMenu,KeyEvent.VK_L,this);  
			deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));  
			
			editMenu.addSeparator();  
			
			findItem=createMenuItem(editFind,KeyEvent.VK_F,editMenu,KeyEvent.VK_F,this);  
			findNextItem=createMenuItem(editFindNext,KeyEvent.VK_N,editMenu,KeyEvent.VK_N,this);  
			findNextItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));  
			replaceItem=createMenuItem(editReplace,KeyEvent.VK_R,editMenu,KeyEvent.VK_H,this);  
			gotoItem=createMenuItem(editGoTo,KeyEvent.VK_G,editMenu,KeyEvent.VK_G,this);  
			
			editMenu.addSeparator();  
			
			selectAllItem=createMenuItem(editSelectAll,KeyEvent.VK_A,editMenu,KeyEvent.VK_A,this);  
			createMenuItem(editTimeDate,KeyEvent.VK_D,editMenu,KeyEvent.VK_D,this)  
			.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
			 
	//--------------------------------------Functions Of Menus--------------------------------------------------------------//	  
			 
			MenuListener editMenuListener=new MenuListener()
			{  
			   public void menuSelected(MenuEvent evvvv)  
			    {  
				   if(Notepad_beta.this.textArea.getText().length()==0)  
				   {  
					   findItem.setEnabled(false);  
					   findNextItem.setEnabled(false);  
					   replaceItem.setEnabled(false);  
					   selectAllItem.setEnabled(false);  
					   gotoItem.setEnabled(false);  
				   }  
				   else  
				   {  
					   findItem.setEnabled(true);  
					   findNextItem.setEnabled(true);  
					   replaceItem.setEnabled(true);  
					   selectAllItem.setEnabled(true);  
					   gotoItem.setEnabled(true);  
				   }  
				   if(Notepad_beta.this.textArea.getSelectionStart()==textArea.getSelectionEnd())  
				   {  
					   cutItem.setEnabled(false);  
					   copyItem.setEnabled(false);  
					   deleteItem.setEnabled(false);  
				   }  
				   else  
				   {  
					   cutItem.setEnabled(true);  
					   copyItem.setEnabled(true);  
					   deleteItem.setEnabled(true);  
				   }  
			    }  
			   
			   public void menuDeselected(MenuEvent evvvv){}  
			   public void menuCanceled(MenuEvent evvvv){} 
			   
			}; 
			
			editMenu.addMenuListener(editMenuListener);  
			 
			
			f.setJMenuBar(mb);
			
		}
		
	//-------------------End of menuListener------------------------------------------------//
		
	//-------------------Creating Menu------------------------------------------------------//
		
		JMenu createMenu(String s, int key, JMenuBar toMenuBar)
		{ 
			JMenu temp = new JMenu(s);
			temp.setMnemonic(key);
			toMenuBar.add(temp);
			return temp;
			
		}
		
	//-----------------Creating Menu Item-------------------------------------------------//
		
		JMenuItem createMenuItem(String r, int key1, JMenu toMenu, int key2,ActionListener al)
		{
			JMenuItem temp = new JMenuItem(r,key1);
			temp.addActionListener(al);

			temp.setAccelerator(KeyStroke.getKeyStroke(key2,ActionEvent.CTRL_MASK));
			toMenu.add(temp);
			return temp;
		}
		
	//---------------End Of Menu----------------------------------------------------------//
		
		
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	new Notepad_beta();
	
	
	}	

//---------------------------------------------------------------------------------------------------------------------//
	void goTo()  
	{  
		int lineNumber=0;  
		try  
		{  
			lineNumber=textArea.getLineOfOffset(textArea.getCaretPosition())+1;
			
			String tempStr=JOptionPane.showInputDialog(f,"Enter Line Number:",""+lineNumber);  
			if(tempStr==null)  
			{
				return;
			}  
			lineNumber=Integer.parseInt(tempStr);  
			
			textArea.setCaretPosition(textArea.getLineStartOffset(lineNumber-1));  
		}catch(Exception e){}  
	}

//---------------------------------------------------------------------------------------------------------------------//

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

		String cmdText=e.getActionCommand();
		
		if(cmdText.equals(fileNew))
			fileHandler.newFile();
		else if(cmdText.equals(fileOpen))
			fileHandler.openFile();
		else if(cmdText.equals(fileSave))
			fileHandler.saveThisFile();
		else if(cmdText.equals(fileSaveAs))
			fileHandler.saveAsFile();
		else if(cmdText.equals(fileExit))
		{
			if(fileHandler.confirmSave())
				System.exit(0);
		}
		else if(cmdText.equals(filePrint))
			JOptionPane.showMessageDialog(
					Notepad_beta.this.f,
					"Get ur printer repaired first! It seems u dont have one!","Bad Printer",
					JOptionPane.INFORMATION_MESSAGE
					);
		else if(cmdText.equals(editCut))
			textArea.cut();
		else if(cmdText.equals(editCopy))
			textArea.copy();	
		else if(cmdText.equals(editPaste))
			textArea.paste();
		else if(cmdText.equals(editDelete))
			textArea.replaceSelection("");
		else if(cmdText.equals(editFind))  
		{  
			if(Notepad_beta.this.textArea.getText().length()==0)  
				return; // text box have no text  
			if(findReplaceDialog==null)  
				findReplaceDialog=new FindDialog(Notepad_beta.this.textArea);  
			findReplaceDialog.showDialog(Notepad_beta.this.f,true);//find  
		}  
		else if(cmdText.equals(editFindNext))  
		{  
			if(Notepad_beta.this.textArea.getText().length()==0)  
				return; // text box have no text  

			if(findReplaceDialog==null)  
				statusBar.setText("Use Find option of Edit Menu first !!!!");  
			else  
				findReplaceDialog.findNextWithSelection();  
		}  
		else if(cmdText.equals(editReplace))  
		{  
			if(Notepad_beta.this.textArea.getText().length()==0)  
				return; // text box have no text  			
			if(findReplaceDialog==null)  
				findReplaceDialog=new FindDialog(Notepad_beta.this.textArea);  
			findReplaceDialog.showDialog(Notepad_beta.this.f,false);//replace  
		}  
		else if(cmdText.equals(editGoTo))  
		{  
			if(Notepad_beta.this.textArea.getText().length()==0)  
				return; // text box have no text  
			goTo();  
		}
		else if(cmdText.equals(editSelectAll))  
			textArea.selectAll();  
		else if(cmdText.equals(editTimeDate))  
			textArea.insert(new Date().toString(),textArea.getSelectionStart());  
	}

}

/////////////////////////////////////
interface AllMenus{

final String fileText="File";
final String editText="Edit";
final String formatText="Format";
final String viewText="View";
final String helpText="Help";

final String fileNew="New";
final String fileOpen="Open...";
final String fileSave="Save";
final String fileSaveAs="Save As...";
final String filePageSetup="Page Setup...";
final String filePrint="Print";
final String fileExit="Exit";

final String editUndo="Undo";
final String editCut="Cut";
final String editCopy="Copy";
final String editPaste="Paste";
final String editDelete="Delete";
final String editFind="Find...";
final String editFindNext="Find Next";
final String editReplace="Replace";
final String editGoTo="Go To...";
final String editSelectAll="Select All";
final String editTimeDate="Time/Date";
	
}

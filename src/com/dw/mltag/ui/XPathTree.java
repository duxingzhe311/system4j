package com.dw.mltag.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.dw.mltag.AbstractNode;
import com.dw.mltag.NodeFilterParser;
import com.dw.mltag.XHtmlPath;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class XPathTree
	extends JFrame
{
	JPanel jPanel2 = new JPanel();
	BorderLayout borderLayout2 = new BorderLayout();
	JPanel jPanel3 = new JPanel();
	JLabel jLabel1 = new JLabel();
	JTextField addressTextField = new JTextField();
	JButton btnAddressInput = new JButton();
	BorderLayout borderLayout3 = new BorderLayout();
	
	XHtmlPath xhtmlPath = null;
	JPanel jPanel4 = new JPanel();
	JPanel jPanel1 = new JPanel();
	JTextField tfPath = new JTextField();
	JButton btnUp = new JButton() ;
	JTree mainTree = new JTree();
	JSplitPane jSplitPane1 = new JSplitPane();
	JScrollPane spMainTree = new JScrollPane();
	BorderLayout borderLayout1 = new BorderLayout();
	BorderLayout borderLayout4 = new BorderLayout();

	MyTreeCellRenderer cr = new MyTreeCellRenderer();
	MyListCellRenderer myListCr = new MyListCellRenderer();

	BorderLayout borderLayout5 = new BorderLayout();
	JScrollPane spContent = new JScrollPane();
	JScrollPane jScrollPane3 = new JScrollPane();
	JList attrList = new JList();
	JPanel jPanel5 = new JPanel();
	BorderLayout borderLayout6 = new BorderLayout();
	JPanel jPanel6 = new JPanel();
	BorderLayout borderLayout7 = new BorderLayout();
	JSplitPane jSplitPane2 = new JSplitPane();
	JPanel jPanel7 = new JPanel();
	JPanel jPanel8 = new JPanel();
	BorderLayout borderLayout8 = new BorderLayout();
	BorderLayout borderLayout9 = new BorderLayout();
	JScrollPane jScrollPane4 = new JScrollPane();
	JList searchResList = new JList();
	JTextField tfSearch = new JTextField();
	JButton btnSearch = new JButton();
	JTextArea taContent = new JTextArea();

	public XPathTree()
	{
		try
		{
			jbInit();
			myInit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		XPathTree xpt = new XPathTree();
		xpt.setSize(800, 600);
		xpt.setVisible(true);
	}

	private void jbInit()
		throws Exception
	{
		jPanel2.setLayout(borderLayout2);
		jLabel1.setText("Address");
		btnAddressInput.setText("ok");
		btnAddressInput.addMouseListener(new
										 XPathTree_btnAddressInput_mouseAdapter(this));
		jPanel3.setLayout(borderLayout3);
		addressTextField.setText("");
		jPanel1.setLayout(borderLayout1);
		tfPath.setText("");
		jPanel4.setLayout(borderLayout4);
		mainTree.addMouseListener(new XPathTree_mainTree_mouseAdapter(this));
		this.getContentPane().setLayout(borderLayout5);
		jScrollPane3.setPreferredSize(new Dimension(260, 80));
		jPanel5.setLayout(borderLayout6);
		jPanel6.setLayout(borderLayout7);
		jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jPanel7.setLayout(borderLayout8);
		jPanel8.setLayout(borderLayout9);
		btnSearch.setPreferredSize(new Dimension(30, 25));
		btnSearch.setText("Search");
		btnSearch.addMouseListener(new XPathTree_btnSearch_mouseAdapter(this));
		tfSearch.setText("");
		searchResList.addMouseListener(new XPathTree_searchResList_mouseAdapter(this));
		this.setTitle("Html½ÚµãÊ°È¡Æ÷");
		this.getContentPane().add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(jPanel3, BorderLayout.NORTH);
		jPanel3.add(jLabel1, BorderLayout.WEST);
		jPanel3.add(addressTextField, BorderLayout.CENTER);
		jPanel3.add(btnAddressInput, BorderLayout.EAST);
		jPanel2.add(jPanel4, BorderLayout.CENTER);
		jPanel4.add(jSplitPane1, BorderLayout.CENTER);
		jPanel5.add(jSplitPane2, BorderLayout.CENTER);
		jSplitPane2.add(jPanel7, JSplitPane.TOP);
		jSplitPane2.add(jPanel8, JSplitPane.BOTTOM);
		jSplitPane1.add(jPanel5, JSplitPane.LEFT);
		jSplitPane1.add(jPanel1, JSplitPane.RIGHT);
		JPanel tmpp = new JPanel() ;
		tmpp.setLayout(new BorderLayout()) ;
		btnUp.setText("^") ;
		tmpp.add(tfPath,BorderLayout.CENTER) ;
		tmpp.add(btnUp,BorderLayout.EAST) ;
		jPanel1.add(tmpp, BorderLayout.NORTH);		
		jPanel1.add(spContent, BorderLayout.CENTER);
		spContent.getViewport().add(taContent, null);
		jPanel1.add(jScrollPane3, BorderLayout.SOUTH);
		jScrollPane3.getViewport().add(attrList, null);
		spMainTree.getViewport().add(mainTree, null);
		jSplitPane1.setDividerLocation(150);
		jPanel7.add(spMainTree, BorderLayout.CENTER);
		jPanel8.add(jPanel6, BorderLayout.NORTH);
		jPanel6.add(tfSearch, BorderLayout.CENTER);
		jPanel6.add(btnSearch, BorderLayout.EAST);
		jPanel8.add(jScrollPane4, BorderLayout.CENTER);
		jScrollPane4.getViewport().add(searchResList, null);
		jSplitPane2.setDividerLocation(400);
	}

	

	private void myInit()
	{
		mainTree.setModel(new DefaultTreeModel(null));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		btnUp.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				try
				{
					String curp = tfPath.getText() ;
					if(curp==null||(curp=curp.trim()).equals(""))
						return ;
					
					AbstractNode an = xhtmlPath.getNode(curp);
					if(an==null)
						return ;
					an = (AbstractNode)an.getParent() ;
					if(an==null)
						return ;
					
					
					DefaultTreeModel tm = (DefaultTreeModel) mainTree.getModel();
					TreeNode[] tns = tm.getPathToRoot(an);
					TreePath tp = new TreePath(tns);
					mainTree.expandPath(tp);
					mainTree.setSelectionPath(tp);
					
					detailNode(an) ;
				}
				catch(Throwable t)
				{
					
				}
			}}) ;
	}

	void constructTree()
	{
		if (xhtmlPath == null)
		{
			mainTree.setModel(new DefaultTreeModel(null));
			return;
		}

		AbstractNode rootan = xhtmlPath.getRootNode();
		DefaultTreeModel tm = new DefaultTreeModel(rootan);
		mainTree.setCellRenderer(cr);
		
		mainTree.setModel(tm);
		

	}

	class TmpFilter implements NodeFilterParser.IFilter
	{
		public boolean needCreateXmlNode(String tagname,Properties attrs)
		{
			return true ;
		}
	}
	
	void btnAddressInput_mousePressed(MouseEvent e)
	{
		InputStream tmpis = null;
		try
		{
			String ttt = addressTextField.getText() ;
			if(ttt==null||ttt.equals(""))
				return ;
			
			String enc = null ;
			if(ttt.toLowerCase().startsWith("http://"))
			{
				URL u = new URL(ttt);
				xhtmlPath = new XHtmlPath(u,new TmpFilter());
			}
			else
			{
				tmpis = new FileInputStream(ttt);
				xhtmlPath = new XHtmlPath(tmpis,new TmpFilter());
			}

			
			//xhtmlPath = new XHtmlPath(tmpis);
			constructTree();
		}
		catch (Throwable ee)
		{
			ee.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error:" + ee.toString());
		}
		finally
		{
			try
			{
				if (tmpis != null)
				{
					tmpis.close();
				}
			}
			catch (Exception e0)
			{}
		}
	}

	private AbstractNode getCurrentSelectNode()
	{
		TreePath tp = mainTree.getSelectionPath();
		if (tp == null)
		{
			return null;
		}
		return (AbstractNode) tp.getLastPathComponent();
	}

	void mainTree_mousePressed(MouseEvent e)
	{
		AbstractNode an = getCurrentSelectNode();
		if (an == null)
		{
			return;
		}
		detailNode(an);
	}

	private void detailNode(AbstractNode an)
	{
		try
		{
			if (an == null || xhtmlPath == null)
			{
				return;
			}
			String str = xhtmlPath.getXPathByNode(an);
			tfPath.setText(str);

			taContent.setText(an.toString(false));
			spContent.getViewport().setViewPosition(new Point(0,0));

			Enumeration en = an.getAtributeNames();
			DefaultListModel lm = new DefaultListModel();
			if (en != null)
			{
				while (en.hasMoreElements())
				{
					MyAttr ma = new MyAttr();
					ma.name = (String) en.nextElement();
					ma.value = an.getAttribute(ma.name);
					lm.addElement(ma);
				}
			}
			this.attrList.setModel(lm);
		}
		catch (Throwable t)
		{
			JOptionPane.showMessageDialog(this, "Error:" + t.getMessage());
		}
	}

	void btnSearch_mousePressed(MouseEvent e)
	{
		if (xhtmlPath == null)
		{
			return;
		}
		String key = tfSearch.getText();
		if (key == null || (key=key.trim()).equals(""))
		{
			return;
		}
		AbstractNode an = null;//getCurrentSelectNode();
		if (an == null)
		{
			an = xhtmlPath.getRootNode();
		}
		AbstractNode[] ans = xhtmlPath.searchAll(an, key);
		DefaultListModel lm = new DefaultListModel();
		for (int i = 0; i < ans.length; i++)
		{
			lm.addElement(ans[i]);
		}
		searchResList.setModel(lm);
		searchResList.setCellRenderer(myListCr);
	}

	void searchResList_mouseClicked(MouseEvent e)
	{
		if (xhtmlPath == null)
		{
			return;
		}
		AbstractNode an = (AbstractNode) searchResList.getSelectedValue();
		if (an == null)
		{
			return;
		}
		DefaultTreeModel tm = (DefaultTreeModel) mainTree.getModel();
		TreeNode[] tns = tm.getPathToRoot(an);
		TreePath tp = new TreePath(tns);
		mainTree.expandPath(tp);
		mainTree.setSelectionPath(tp);

		//JScrollPane sp = (JScrollPane)mainTree.getPar
		Rectangle r = mainTree.getPathBounds(tp);

//		spMainTree.getViewport().scrollRectToVisible();
		JViewport vp = spMainTree.getViewport();

		vp.setViewPosition(new Point(r.x, r.y + vp.getSize().height / 2));
		mainTree.repaint();
		detailNode(an);
	}
}

class XPathTree_btnAddressInput_mouseAdapter
	extends java.awt.event.MouseAdapter
{
	XPathTree adaptee;

	XPathTree_btnAddressInput_mouseAdapter(XPathTree adaptee)
	{
		this.adaptee = adaptee;
	}

	public void mousePressed(MouseEvent e)
	{
		adaptee.btnAddressInput_mousePressed(e);
	}
}

class MyTreeCellRenderer
	extends DefaultTreeCellRenderer
{
	public Component getTreeCellRendererComponent(JTree tree, Object value,
												  boolean selected,
												  boolean expanded,
												  boolean leaf, int row,
												  boolean hasFocus)
	{
		AbstractNode an = (AbstractNode) value;

		Component com = super.getTreeCellRendererComponent(tree, an.getNodeName(),
			selected, expanded, leaf, row,
			hasFocus);
//		JLabel tmpjb = (JLabel)com ;
//		tmpjb.setText(an.getNodeName());
		return com;
	}
}

class MyListCellRenderer
	extends JLabel
	implements ListCellRenderer
{
	public MyListCellRenderer()
	{
		setOpaque(true);
	}

	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus)
	{
		AbstractNode an = (AbstractNode) value;
		setText(an.getNodeName());
		setBackground(isSelected ? Color.red : Color.white);
		setForeground(isSelected ? Color.white : Color.black);
		return this;
	}
}

class XPathTree_mainTree_mouseAdapter
	extends java.awt.event.MouseAdapter
{
	XPathTree adaptee;

	XPathTree_mainTree_mouseAdapter(XPathTree adaptee)
	{
		this.adaptee = adaptee;
	}

	public void mousePressed(MouseEvent e)
	{
		adaptee.mainTree_mousePressed(e);
	}
}

class XPathTree_btnSearch_mouseAdapter
	extends java.awt.event.MouseAdapter
{
	XPathTree adaptee;

	XPathTree_btnSearch_mouseAdapter(XPathTree adaptee)
	{
		this.adaptee = adaptee;
	}

	public void mousePressed(MouseEvent e)
	{
		adaptee.btnSearch_mousePressed(e);
	}
}

class XPathTree_searchResList_mouseAdapter
	extends java.awt.event.MouseAdapter
{
	XPathTree adaptee;

	XPathTree_searchResList_mouseAdapter(XPathTree adaptee)
	{
		this.adaptee = adaptee;
	}

	public void mouseClicked(MouseEvent e)
	{
		adaptee.searchResList_mouseClicked(e);
	}
}

class MyAttr
{
	public String name = null;
	public String value = null;
	public String toString()
	{
		return name + "=" + value;
	}
}
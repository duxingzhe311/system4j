var openedid;
var openedid_ft;
var flag=0,sflag=0;

//-------- 菜单点击事件 -------
function myclick(srcelement)
{
  var targetid,srcelement,targetelement;
  var strbuf;

  //-------- 如果点击了展开或收缩按钮---------
  if(srcelement.className=="outline")
  {
     //if(srcelement.title!="" && srcelement.src.indexOf("plus")>-1)
       // menu_shrink();

     targetid=srcelement.id+"d";
     targetelement=document.all(targetid);

     if (targetelement.style.display=="none")
     {
        targetelement.style.display='';
        strbuf=srcelement.src;
        if(strbuf.indexOf("plus.gif")>-1)
                srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_minus.gif";
        else
                srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_minusl.gif";
     }
     else
     {
        targetelement.style.display="none";
        strbuf=srcelement.src;
        if(strbuf.indexOf("minus.gif")>-1)
                srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_plus.gif";
        else
                srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_plusl.gif";
     }
  }
}

//-------- 打开网址 -------
function openURL(URL)
{
    parent.openURL(URL);
}

//-------- 菜单全部展开/收缩 -------
var menu_flag=0;
function menu_expand()
{
  if(menu_flag==0)
     expand_text.innerHTML="收缩";
  else
     expand_text.innerHTML="展开";

  menu_flag=1-menu_flag;

  for (i=0; i<document.all.length; i++)
  {
        srcelement=document.all(i);
        if(srcelement.className=="outline")
        {
                targetid=srcelement.id+"d";
                targetelement=document.all(targetid);
                if (menu_flag==1)
                {
                        targetelement.style.display='';
                        strbuf=srcelement.src;
                        if(strbuf.indexOf("plus.gif")>-1)
                                srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_minus.gif";
                        else
                                srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_minusl.gif";
                }
                else
                {
                        targetelement.style.display="none";
                        strbuf=srcelement.src;
                        if(strbuf.indexOf("minus.gif")>-1)
                                srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_plus.gif";
                        else
                                srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_plusl.gif";
                }
        }
  }
}

//-------- 收缩打开的主菜单项 -------
function menu_shrink()
{
  for (i=0; i<document.all.length; i++)
  {
        srcelement=document.all(i);
        if(srcelement.title!="")
        {
              strbuf=srcelement.src;

              if(strbuf.indexOf("minus")>-1)
              {
                 targetid=srcelement.id+"d";
                 targetelement=document.all(targetid);
                 targetelement.style.display='none';

                 if(strbuf.indexOf("minus.gif")>-1)
                    srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_plus.gif";
                 else
                    srcelement.src="/WebRes?r=/com/dw/web_ui/res/tree_plusl.gif";
               }
         }
  }
}

//-------- 打开windows程序 -------
function winexe(NAME,PROG)
{
   URL="/general/winexe?PROG="+PROG+"&NAME="+NAME;
   window.open(URL,"winexe","height=100,width=350,status=0,toolbar=no,menubar=no,location=no,scrollbars=yes,top=0,left=0,resizable=no");
}
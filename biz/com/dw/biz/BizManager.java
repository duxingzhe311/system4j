package com.dw.biz;

import com.dw.biz.api.BizFuncRunResult;
import com.dw.biz.platform.BizActionRtCompilerEnv;
import com.dw.biz.platform.BizActionRunner;
import com.dw.biz.platform.BizFormRtCompilerEnv;
import com.dw.biz.platform.BizParam;
import com.dw.biz.platform.BizRunner;
import com.dw.biz.platform.BizViewCellRtCompilerEnv;
import com.dw.biz.platform.BizViewCellRunner;
import com.dw.biz.platform.BizViewRtCompilerEnv;
import com.dw.biz.platform.BizViewRunner;
import com.dw.biz.platform.compiler.BizActionRuntime;
import com.dw.biz.platform.compiler.BizViewCellRuntime;
import com.dw.biz.platform.compiler.BizViewRuntime;
import com.dw.biz.platform.compiler.RuntimeCompiler;
import com.dw.system.AppConfig;
import com.dw.system.gdb.datax.DataXManager;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import com.dw.user.right.RightManager;
import com.dw.user.right.RightRule;
import com.dw.user.sso.SSOManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BizManager
{
  static String[] BizActionTypeClassNames = { 
    "com.dw.biz.JavaJspBAT" };

  static String[] BizViewTypeClassNames = { 
    "com.dw.biz.ShowBVT", "com.dw.biz.FormBVT", 
    "com.dw.biz.ComposeBVT" };
  public static final String BIZ_NODETYPE_VIEW = "view";
  public static final String BIZ_NODETYPE_VIEWCELL = "view_cell";
  public static final String BIZ_NODETYPE_ACTION = "action";
  public static final String BIZ_NODETYPE_FUNC = "func";
  public static final String BIZ_NODETYPE_FORM = "form";
  public static final String BIZ_NODETYPE_WORKITEM_TEMP = "wi_temp";
  public static final String BIZ_NODETYPE_FLOW = "flow";
  static Hashtable<String, Class> bizNodeType2Class = new Hashtable();

  private static Hashtable<String, BizActionType> name2actionType = new Hashtable();

  private static BizActionType[] allBizActionTypes = null;

  static BizViewType defaultViewType = null;

  private static Hashtable<String, BizViewType> name2viewType = new Hashtable();

  private static BizViewType[] allBizViewTypes = null;

  static ILogger log = LoggerManager.getLogger(BizManager.class
    .getCanonicalName());

  static BizManager bizMgr = null;
  static Object locker = new Object();

  private transient DataXManager dataxMgr = null;

  private transient RightManager rightMgr = null;

  private transient SSOManager ssoMgr = null;

  private BizContainer bizContainer = null;

  static
  {
    bizNodeType2Class.put("view", BizView.class);
    bizNodeType2Class.put("view_cell", BizViewCell.class);
    bizNodeType2Class.put("action", BizAction.class);
    bizNodeType2Class.put("func", BizFunc.class);
    bizNodeType2Class.put("form", BizForm.class);
    bizNodeType2Class
      .put("wi_temp", BizWorkItemTemp.class);
    bizNodeType2Class.put("flow", BizFlow.class);
  }

  private void initBizVAType()
  {
    ArrayList lls = new ArrayList();
    for (String cn : BizActionTypeClassNames)
    {
      try
      {
        Class c = Class.forName(cn);
        BizActionType bat = (BizActionType)c.newInstance();

        bat.init(this, getDataXManager());

        name2actionType.put(bat.getTypeName(), bat);
        lls.add(bat);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        log.error(e);
      }
    }

    allBizActionTypes = new BizActionType[lls.size()];
    lls.toArray(allBizActionTypes);

    ArrayList bvts = new ArrayList();
    for (String cn : BizViewTypeClassNames)
    {
      try
      {
        Class c = Class.forName(cn);
        BizViewType bvt = (BizViewType)c.newInstance();

        name2viewType.put(bvt.getTypeName(), bvt);
        bvts.add(bvt);
      }
      catch (Exception e)
      {
        e.printStackTrace();
        log.error(e);
      }
    }

    allBizViewTypes = new BizViewType[bvts.size()];
    bvts.toArray(allBizViewTypes);
    defaultViewType = allBizViewTypes[0];
  }

  public static BizActionType[] getAllBizActionTypes()
  {
    return allBizActionTypes;
  }

  public static BizActionType getBizActionType(String typename)
  {
    return (BizActionType)name2actionType.get(typename);
  }

  public static BizViewType[] getAllBizViewTypes()
  {
    return allBizViewTypes;
  }

  public static BizViewType getBizViewType(String typename)
  {
    BizViewType bvt = (BizViewType)name2viewType.get(typename);
    if (bvt != null)
      return bvt;
    return defaultViewType;
  }

  public static Class getBizNodeTypeClass(String typen)
  {
    if (typen == null) {
      return null;
    }
    return (Class)bizNodeType2Class.get(typen);
  }

  public static boolean isBizViewPath(String strpath)
  {
    BizPath bp = new BizPath(strpath);
    return "view".equals(bp.getNodeType());
  }

  public static boolean isBizViewCellPath(String strpath)
  {
    BizPath bp = new BizPath(strpath);
    return "view_cell".equals(bp.getNodeType());
  }

  public static boolean isBizActionPath(String strpath)
  {
    BizPath bp = new BizPath(strpath);
    return "action".equals(bp.getNodeType());
  }

  public static void checkBizName(String bizname)
  {
    StringBuffer tmpsb = new StringBuffer();
    if (!checkBizName(bizname, tmpsb))
      throw new IllegalArgumentException("illegal biz name:" + 
        tmpsb.toString());
  }

  public static boolean checkBizName(String pname, StringBuffer failedreson)
  {
    if ((pname == null) || (pname.equals("")))
    {
      failedreson.append("name cannot be null or empty!");
      return false;
    }

    char c0 = pname.charAt(0);
    if (((c0 < 'a') || (c0 > 'z')) && ((c0 < 'A') || (c0 > 'Z')) && (c0 != '_'))
    {
      failedreson.append("name first char must be a-z|A-Z|_");
      return false;
    }

    int len = pname.length();
    for (int i = 1; i < len; i++)
    {
      char c = pname.charAt(i);
      if (((c < 'a') || (c > 'z')) && ((c < 'A') || (c > 'Z')) && 
        ((c < '0') || (c > '9')) && (c != '_'))
      {
        failedreson.append("name char must be a-z|A-Z|0-9|_");
        return false;
      }
    }

    return true;
  }

  public static BizManager getInstance()
  {
    if (bizMgr != null) {
      return bizMgr;
    }
    synchronized (locker)
    {
      if (bizMgr != null) {
        return bizMgr;
      }
      try
      {
        bizMgr = new BizManager();
        return bizMgr;
      }
      catch (Exception e)
      {
        log.error(e);
        return null;
      }
    }
  }

  private BizManager()
    throws Exception
  {
    String webappb = AppConfig.getTomatoWebappBase();
    this.bizContainer = new DirBaseBizContainer(webappb, this);

    this.bizContainer.initContainer();

    this.dataxMgr = DataXManager.getInstance();

    initBizVAType();

    this.rightMgr = RightManager.getDefaultIns();

    this.ssoMgr = this.rightMgr.getSSOManager();
  }

  public RightManager getRightManager()
  {
    return this.rightMgr;
  }

  public BizContainer getBizContainer()
  {
    return this.bizContainer;
  }

  public DataXManager getDataXManager()
  {
    return this.dataxMgr;
  }

  public BizNode getBizNodeByPath(String path) throws Exception
  {
    BizPath bizp = new BizPath(path);
    if (!bizp.isNode())
    {
      return null;
    }
    return this.bizContainer.getBizNode(bizp);
  }

  public BizView getBizViewByPath(String vpath) throws Exception
  {
    BizPath bizp = new BizPath(vpath);

    if (!bizp.isNode())
    {
      return null;
    }

    BizNode bn = this.bizContainer.getBizNode(bizp);
    if (bn == null)
    {
      return null;
    }

    if (bizp.isInner())
    {
      BizNodeObj bno = bn.getBizObj();
      if ((bno instanceof IBizHasInnerView))
      {
        return ((IBizHasInnerView)bno).getInnerViewById(bizp.getInnerId());
      }

    }

    return (BizView)bn.getBizObj();
  }

  public List<BizAction> getBizViewRelatedActions(BizView bv)
    throws Exception
  {
    String bvp = bv.getBizPathStr();
    BizNode bvbn = bv.getBelongToBizNode();
    List bns = bvbn.getBelongToNodeContainer().getSubNodesByPrefixType(bvbn.getNodeFileName(), "action");
    if (bns == null) {
      return null;
    }
    ArrayList bas = new ArrayList(bns.size());
    for (BizNode bn : bns)
    {
      bas.add((BizAction)bn.getBizObj());
    }
    return bas;
  }

  public BizViewCell getBizViewCellByPath(String vpath) throws Exception
  {
    BizPath bizp = new BizPath(vpath);
    if (!bizp.isNode()) {
      throw new IllegalArgumentException("path=" + vpath + 
        " is not node!");
    }
    if (!"view_cell".equals(bizp.getNodeType()))
    {
      throw new IllegalArgumentException("path=" + vpath + 
        " is not view cell path");
    }

    BizNode bn = this.bizContainer.getBizNode(bizp);
    if (bn == null) {
      throw new IllegalArgumentException("cannot find BizNode with path=" + 
        vpath);
    }
    return (BizViewCell)bn.getBizObj();
  }

  public BizAction getBizActionByPath(String apath)
    throws Exception
  {
    BizPath bizp = new BizPath(apath);
    if (!bizp.isNode()) {
      return null;
    }

    if (!"action".equals(bizp.getNodeType()))
    {
      return null;
    }

    BizNode bn = this.bizContainer.getBizNode(bizp);
    if (bn == null)
    {
      return null;
    }
    return (BizAction)bn.getBizObj();
  }

  public BizFunc getBizFuncByPath(String funcpath) throws Exception
  {
    BizPath bizp = new BizPath(funcpath);
    if (!bizp.isNode()) {
      throw new IllegalArgumentException("path=" + funcpath + 
        " is not node!");
    }
    if (!"func".equals(bizp.getNodeType()))
    {
      throw new IllegalArgumentException("path=" + funcpath + 
        " is not func path");
    }

    BizNode bn = this.bizContainer.getBizNode(bizp);
    if (bn == null) {
      return null;
    }

    return (BizFunc)bn.getBizObj();
  }

  public ArrayList<BizFunc> getAllBizFuncs() throws Exception
  {
    List bns = this.bizContainer.getAllBizNodeByType("func");
    ArrayList bfs = new ArrayList(bns.size());
    for (BizNode bn : bns)
    {
      BizFunc bf = (BizFunc)bn.getBizObj();
      if (bf != null)
      {
        bfs.add(bf);
      }
    }
    return bfs;
  }

  public BizFlow getBizFlowByPath(String fpath)
    throws Exception
  {
    BizPath bizp = new BizPath(fpath);
    if (!bizp.isNode()) {
      throw new IllegalArgumentException("path=" + fpath + 
        " is not node!");
    }
    if (!"flow".equals(bizp.getNodeType()))
    {
      throw new IllegalArgumentException("path=" + fpath + 
        " is not func path");
    }

    BizNode bn = this.bizContainer.getBizNode(bizp);
    if (bn == null) {
      return null;
    }

    return (BizFlow)bn.getBizObj();
  }

  public ArrayList<BizFlow> getAllBizFlows() throws Exception
  {
    List bns = this.bizContainer.getAllBizNodeByType("flow");
    ArrayList bfs = new ArrayList(bns.size());
    for (BizNode bn : bns)
    {
      BizFlow bf = (BizFlow)bn.getBizObj();
      if (bf != null)
      {
        bfs.add(bf);
      }
    }
    return bfs;
  }

  public ArrayList<BizFlow> getBizFlowsWithViewStartByUser(UserProfile up)
    throws Exception
  {
    ArrayList bfs = getAllBizFlows();

    ArrayList rets = new ArrayList();
    for (BizFlow bf : bfs)
    {
      BizFlow.NodeStart ns = bf.getStartNode();
      if (ns != null)
      {
        BizView bv = ns.getBizView();
        if (bv != null)
        {
          BizFlow.NodePerformer np = ns.getRelatedNodePerformer();
          if (np != null)
          {
            BizParticipant bp = np.getParticipant();
            if (bp != null)
            {
              RightRule rr = bp.getContAsRightRule();
              if (rr != null)
              {
                if (up.checkRightRule(rr))
                  rets.add(bf); 
              }
            }
          }
        }
      }
    }
    return rets;
  }

  public BizWorkItemTemp getBizWorkItemTempByPath(String wipath)
    throws Exception
  {
    BizPath bizp = new BizPath(wipath);
    if (!bizp.isNode()) {
      throw new IllegalArgumentException("path=" + wipath + 
        " is not node!");
    }
    if (!"wi_temp".equals(bizp.getNodeType()))
    {
      throw new IllegalArgumentException("path=" + wipath + 
        " is not workitem temp path");
    }

    BizNode bn = this.bizContainer.getBizNode(bizp);
    if (bn == null) {
      throw new IllegalArgumentException("cannot find BizNode with path=" + 
        wipath);
    }
    return (BizWorkItemTemp)bn.getBizObj();
  }

  public void RT_getBizFormCont(PrintWriter outputs, String formpath, BizParam bp)
    throws Exception
  {
    BizPath bizp = new BizPath(formpath);
    if (!bizp.isNode()) {
      throw new IllegalArgumentException("path=" + formpath + 
        " is not node!");
    }
    if (!"form".equals(bizp.getNodeType()))
    {
      throw new IllegalArgumentException("path=" + formpath + 
        " is not form path");
    }

    BizNode bn = this.bizContainer.getBizNode(bizp);
    if (bn == null) {
      throw new IllegalArgumentException("cannot find BizNode with path=" + 
        formpath);
    }
    BizForm bf = (BizForm)bn.getBizObj();
    if (bf == null) {
      throw new Exception("cannot get BizForm with path=" + formpath);
    }
    if (bf.getFormType() != BizForm.FormType.jsp) {
      throw new Exception("not jsp type form!");
    }
    BizFormRtCompilerEnv brce = new BizFormRtCompilerEnv(bf);

    RuntimeCompiler rc = new RuntimeCompiler(brce);
    BizRunner brv = (BizRunner)rc.getJspInstance();

    brv.render(outputs);
  }

  public void RT_getBizViewCont(HttpServletRequest req, HttpServletResponse resp, UserProfile up, String viewpath, XmlData inputxd, PrintWriter outputs)
    throws Exception
  {
    String uid = UUID.randomUUID().toString().replaceAll("-", "");
    RT_getBizViewCont(req, resp, up, uid, viewpath, inputxd, outputs);
  }

  public void RT_getBizViewCont(HttpServletRequest req, HttpServletResponse resp, UserProfile up, String viewid, String viewpath, XmlData inputxd, PrintWriter outputs)
    throws Exception
  {
    BizPath bizp = new BizPath(viewpath);
    if (!bizp.isNode()) {
      throw new IllegalArgumentException("path=" + viewpath + 
        " is not node!");
    }
    if (!"view".equals(bizp.getNodeType()))
    {
      throw new IllegalArgumentException("path=" + viewpath + 
        " is not view path");
    }

    BizNode bn = this.bizContainer.getBizNode(bizp);
    if (bn == null) {
      throw new IllegalArgumentException("cannot find BizNode with path=" + 
        viewpath);
    }
    BizView bv = (BizView)bn.getBizObj();
    if (bv == null) {
      throw new Exception("cannot get BizView with path=" + viewpath);
    }
    RT_getBizViewCont(req, resp, up, viewid, bv, outputs, inputxd);
  }

  private void RT_getBizViewCont(HttpServletRequest req, HttpServletResponse resp, UserProfile up, String uid, BizView bv, PrintWriter outputs, XmlData inputxd)
    throws Exception
  {
    BizViewRunner bvr = RT_getBizViewRunner(uid, bv);
    bvr.prepareRunner(req, resp, up, outputs, 
      this, inputxd);
    bvr.runIt();
  }

  public BizViewRunner RT_getBizViewRunner(String viewid, String viewpath)
    throws Exception
  {
    BizView bv = getBizViewByPath(viewpath);

    if (bv == null) {
      return null;
    }
    return RT_getBizViewRunner(viewid, bv);
  }

  public BizViewRunner createBizViewRunnerIns(BizView bv) throws Exception
  {
    BizViewRtCompilerEnv brce = new BizViewRtCompilerEnv(this, bv);

    BizViewRuntime rc = new BizViewRuntime(this, brce);
    return (BizViewRunner)rc.getJspInstance();
  }

  public BizViewRunner RT_getBizViewRunner(String viewid, BizView bv) throws Exception
  {
    BizViewRunner bvr = createBizViewRunnerIns(bv);
    if (bvr == null) {
      return null;
    }
    bvr.setViewId(viewid);

    return bvr;
  }

  public BizViewCellRunner createBizViewCellRunnerIns(BizViewCell bvc)
    throws Exception
  {
    BizViewCellRtCompilerEnv brce = new BizViewCellRtCompilerEnv(bvc);

    BizViewCellRtCompilerEnv bvc_env = new BizViewCellRtCompilerEnv(bvc);
    BizViewCellRuntime bvcr = new BizViewCellRuntime(bvc_env);

    return (BizViewCellRunner)bvcr.getJspInstance();
  }

  public BizViewCellRunner createBizViewCellRunnerIns(String bvcp)
    throws Exception
  {
    BizViewCell bvc = getBizViewCellByPath(bvcp);
    if (bvc == null) {
      throw new IllegalArgumentException("no biz view cell found with path=" + bvcp);
    }
    return createBizViewCellRunnerIns(bvc);
  }

  public BizActionResult RT_doBizAction(UserProfile up, String actionpath, XmlData inputxd)
    throws Exception
  {
    return RT_doBizAction(up, actionpath, null, inputxd);
  }

  public BizActionResult RT_doBizAction(UserProfile up, String actionpath, XmlData ctrlxd, XmlData inputxd)
    throws Exception
  {
    return RT_doBizAction(up, actionpath, ctrlxd, 
      inputxd, null);
  }

  public BizActionResult RT_doBizAction(UserProfile up, String actionpath, XmlData ctrlxd, XmlData inputxd, HashMap<String, Object> inputom)
    throws Exception
  {
    int k = actionpath.lastIndexOf('#');
    if (k > 0)
    {
      actionpath = actionpath.substring(0, k);
    }

    int p = actionpath.indexOf('?');
    if (p > 0)
    {
      String fixp = actionpath.substring(p + 1);
      XmlData tmpxd = XmlData.parseFromUrlStr(fixp);
      if (tmpxd != null)
      {
        if (inputxd != null) {
          tmpxd.combineAppend(inputxd);
        }
        inputxd = tmpxd;
      }
      actionpath = actionpath.substring(0, p);
    }
    BizPath bizp = new BizPath(actionpath);
    if (!bizp.isNode()) {
      throw new IllegalArgumentException("path=" + actionpath + 
        " is not node!");
    }
    if (!"action".equals(bizp.getNodeType()))
    {
      throw new IllegalArgumentException("path=" + actionpath + 
        " is not action path");
    }

    BizNode bn = this.bizContainer.getBizNode(bizp);
    if (bn == null) {
      throw new IllegalArgumentException("cannot find BizNode with path=" + 
        actionpath);
    }
    BizAction ba = (BizAction)bn.getBizObj();
    if (ba == null) {
      throw new Exception("cannot get BizAction with path=" + actionpath);
    }
    return RT_doBizAction(up, null, null, ba, ctrlxd, inputxd, inputom);
  }

  public BizActionRunner getBizActionRunnerObj(BizAction ba)
    throws Exception
  {
    BizActionRtCompilerEnv brce = new BizActionRtCompilerEnv(ba);

    BizActionRuntime rc = new BizActionRuntime(this, brce);
    return (BizActionRunner)rc.getJspInstance();
  }

  BizActionResult RT_doBizAction(UserProfile up, BizTransaction bt, IBizEnv env, BizAction ba, XmlData ctrlxd, XmlData inputxd)
    throws Exception
  {
    return RT_doBizAction(up, bt, env, 
      ba, ctrlxd, inputxd, null);
  }

  BizActionResult RT_doBizAction(UserProfile up, BizTransaction bt, IBizEnv env, BizAction ba, XmlData ctrlxd, XmlData inputxd, HashMap<String, Object> inputom)
    throws Exception
  {
    BizActionRunner brv = getBizActionRunnerObj(ba);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    brv.prepareRunner(up, null, pw, this, env, ba.getBizPathStr(), 
      bt, ctrlxd, inputxd, inputom);
    brv.runIt();

    XmlData outxd = brv.getOutputXmlData();

    return new BizActionResult(sw.toString().trim(), outxd, brv.getOutputObjMap());
  }

  public BizFuncRunResult RT_runBizFunc(String usern, String funcpath, XmlData inputxd)
    throws Exception
  {
    return RT_runBizFunc(null, usern, funcpath, inputxd);
  }

  public BizFuncRunResult RT_runBizFunc(IBizEnv env, String usern, String funcpath, XmlData inputxd)
    throws Exception
  {
    BizFunc bf = getBizFuncByPath(funcpath);
    if (bf == null) {
      throw new Exception("cannot get BizFunc with path=" + funcpath);
    }
    BizFunc.INode tmpn = bf.getStartNode();
    if (tmpn == null)
    {
      return BizFuncRunResult.createErrorResult(funcpath, 0, 
        "no start node found");
    }

    UserProfile up = this.ssoMgr.accessLoginUserProfile(usern);
    return RT_runBizFunc(up, null, env, bf, tmpn, inputxd);
  }

  public BizFuncRunResult RT_runBizFunc(IBizEnv env, UserProfile up, BizFunc bf, XmlData inputxd)
    throws Exception
  {
    BizFunc.INode tmpn = bf.getStartNode();
    if (tmpn == null)
    {
      return BizFuncRunResult.createErrorResult(bf.getBizPathStr(), 0, 
        "no start node found");
    }

    return RT_runBizFunc(up, null, env, bf, tmpn, inputxd);
  }

  private BizFuncRunResult RT_runBizFunc(UserProfile up, BizTransaction bt, IBizEnv env, BizFunc bf, BizFunc.INode runnode, XmlData inputxd)
    throws Exception
  {
    do {
      if ((runnode instanceof BizFunc.NodeAction))
      {
        BizFunc.NodeAction na = (BizFunc.NodeAction)runnode;
        BizAction ba = na.getBizAction();
        BizActionResult bar = RT_doBizAction(up, bt, env, ba, null, inputxd);

        String resstr = bar.getResultStr();
        inputxd = bar.getResultData();
        runnode = bf.getOutputNode(na, resstr);

        if (runnode == null)
        {
          return BizFuncRunResult.createEndResult(bf.getBizPath()
            .toString(), null, null);
        }
      } else {
        if ((runnode instanceof BizFunc.NodeView))
        {
          BizFunc.NodeView nv = (BizFunc.NodeView)runnode;
          BizView bv = nv.getBizView();

          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          String uid = UUID.randomUUID().toString().replaceAll("-", "");
          RT_getBizViewCont(null, null, up, uid, bv, pw, inputxd);
          pw.flush();

          return BizFuncRunResult.createShowBizViewResult(bf.getBizPath()
            .toString(), bv.getBizPathStr(), sw.toString());
        }

        return BizFuncRunResult.createErrorResult(bf.getBizPath()
          .toString(), 0, "unknown node!");
      }
    }
    while (runnode != null);

    return BizFuncRunResult.createErrorResult(bf.getBizPath().toString(), 
      0, "unknown node!");
  }

  public BizFuncRunResult RT_runBizFuncBizViewSubmit(String usern, String funcpath, String bizviewpath, String oper, XmlData submitxd)
    throws Exception
  {
    BizFunc bf = getBizFuncByPath(funcpath);
    if (bf == null) {
      throw new Exception("cannot get BizFunc with path=" + funcpath);
    }
    BizFunc.NodeView nv = bf.getNodeViewByViewPath(bizviewpath);

    if (nv == null) {
      return BizFuncRunResult.createErrorResult(funcpath, 0, 
        "no View Node with path=" + bizviewpath);
    }
    BizFunc.INode nextn = bf.getOutputNode(nv, oper);
    if (nextn == null) {
      return BizFuncRunResult.createEndResult(funcpath, null, null);
    }
    UserProfile up = this.ssoMgr.accessLoginUserProfile(usern);
    return RT_runBizFunc(up, null, null, 
      bf, nextn, submitxd);
  }

  public BizFuncRunResult RT_runBizFuncBizViewSubmit(UserProfile up, BizFunc bf, BizView cur_bv, String oper, XmlData submitxd)
    throws Exception
  {
    BizFunc.NodeView nv = bf.getNodeViewByViewPath(cur_bv.getBizPathStr());

    if (nv == null) {
      return BizFuncRunResult.createErrorResult(bf.getBizPathStr(), 0, 
        "no View Node with path=" + cur_bv.getBizPathStr());
    }
    BizFunc.INode nextn = bf.getOutputNode(nv, oper);
    if (nextn == null) {
      return BizFuncRunResult.createEndResult(bf.getBizPathStr(), null, 
        null);
    }
    return RT_runBizFunc(up, null, null, 
      bf, nextn, submitxd);
  }
}

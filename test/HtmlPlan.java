import com.dw.system.Convert;
import com.dw.system.util.HTMLUtil;


public class HtmlPlan {
	public static void main(String[] args) {
		System.out.println(Convert.htmlToPlain0("<script>alert(100);</script>"));
		System.out.println(Convert.htmlToPlain("<script>alert(100);</script>"));
		System.out.println(Convert.plainToJsStr("<script>alert(100);</script>"));
		System.out.println(HTMLUtil.stripHTMLTags("<script>alert(100);</script>"));
		System.out.println(HTMLUtil.escapeHTML("<script>alert(100);</script>"));
	}
}

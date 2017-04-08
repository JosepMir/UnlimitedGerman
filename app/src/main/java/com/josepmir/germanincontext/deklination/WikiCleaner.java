package com.josepmir.germanincontext.deklination;

import com.josepmir.germanincontext.util.HttpGetter;
import com.josepmir.germanincontext.util.StringExtensions;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by josep on 23.04.14.
 */
public class WikiCleaner {

    public static String GetWikiContent(String name) {
        name = name.replace(" ", "%20");
        try {

            //String content = new ReadTask().execute("http://de.wikipedia.org/w/api.php?format=xml&action=query&titles=" + name + "&prop=extracts&explaintext&exsectionformat=plain&exintro").get();
            String content = new HttpGetter().execute("http://de.wikipedia.org/w/api.php?format=xml&action=query&titles=" + name + "&prop=extracts&explaintext&exsectionformat=plain&exchars=6000").get();
            return CleanWikipediaArticleResponse(content);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return "Error while getting Wiki";
    }

    public static String GetStupidediaContent(String name) {
        name = name.replace(" ", "%20");
        //return CleanStupidediaArticleResponse("");
        try {
            String content = new HttpGetter().execute("http://www.stupidedia.org/api.php?format=xml&action=query&export&titles=" + name).get();
            return CleanStupidediaArticleResponse(content);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return "Error while getting Wiki";

    }

    public static String CleanStupidediaArticleResponse(String text) {
        try {
            //look for <text xml:space=          until </text>

            text = text.replace("&amp;", "&"); //Bildergalerie == &lt;gallery    and not     Bildergalerie == &amp;lt;gallery

            text = text.replace("&lt;", "<").replace("&gt;", ">");
            try { //well, if stupidedia someday changes formatting of API, at least show sthing and not crash everything
                text = text.substring(text.indexOf("<page>"));
                text = text.substring(text.indexOf("<text "));
                text = text.substring(text.indexOf(">") + 1);
                text = text.substring(0, text.indexOf("</text>"));
            } catch (Exception ex) {
                text += "(internal error: Stupidedia wiki format changed)";
            }
            //------------------


            text = text.replace("\\{\\{\\{", ""); //{{{ well, such nice thing  are "template parameters". we can delete them outright
            text = text.replace("\\}\\}\\}", ""); //}}} well, such nice thing are "template parameters".


            //remove wikicode
            //Solving this kind of problem by regexp is not right. It's the same as matching brackets - difficult to do with regexp. Regexps are not suitable for nested expressions in general.
            text = removeTemplates(text, "{{", "}}"); //templates
            text = removeTemplates(text, "{|", "|}"); //tables

            text = removeDatei(text, "Datei");
            text = removeDatei(text, "Kategorie");
            text = removeDatei(text, "Sub");

            text = removeWikilinks(text);

            text = text.replace("<u>","*").replace("</u>","*");

            text = text.replace("'''","");
            text = text.replace("&quot;","\"");
            text = text.replace("<br/>","\n").replace("<br />","\n");

            return text;

            //only at the end replace &quot;, after ''' are processed...
            //.replaceAll("&quot;", "'");
        } catch (Exception ex) {
            return "Error on CleanStupidediaArticleResponse()" + ex.toString();
        }
    }

    private static String removeWikilinks(String text) {
        //text = "aaa [[Beatles]] bbb [[Oasis (band)|Oasis]] nanan";
        String startSeq = "[[";
        StringBuilder result = new StringBuilder();
        boolean insideDatei = false;
        int level = 0;
        while (!text.equals("")) {


            int start = text.indexOf("[[");

            if (start != -1) {
                result.append(text.substring(0, start));
                text = text.substring(start + 2);

                int end = text.indexOf("]]");
                String link = text.substring(0, end);

                if (link.contains("|")) {
                    int pal = link.lastIndexOf("|");
                    link = link.substring(pal + 1);
                }
                result.append(link);

                text = text.substring(end + 2);

            } else { //no more links
                result.append(text);
                text = "";
            }


        }

        return result.toString();

    }


    private static String removeDatei(String text, String startSeq) {
        //UNIT TEST
        //text = ", [[nicht]] Geld. [[Datei:Its alive.jpg|thumb|right|350px|Die [[Beatles]], auch [[Pilzköpfe]] oder ''Fungus Four'' genannt, revolutionierten die Musik und die Haarmode.]] ==";
        startSeq = "[[" + startSeq + ":";

        StringBuilder result = new StringBuilder();
        boolean insideDatei = false; //to ignore [[nicht]]
        int level = 0;
        while (!text.equals("")) {

            //speed up search //England case: from 9 seconds to 0!!!!! the same for removeTemplates...amazing
            if (!insideDatei) {
                int beginKey = text.indexOf(startSeq);
                if (beginKey == -1) {//no more findings till the end
                    result.append(text); //copy what's left
                    return result.toString(); //and exit
                }
                result.append(text.substring(0, beginKey));
                text = text.substring(beginKey);
            }

            String watchout = text;
            if (watchout.startsWith(startSeq)) {
                level++;
                insideDatei = true;
            } else if (insideDatei && watchout.startsWith("[[")) { //for links inside [[Datei:  ]]
                level++;
            }

            if (watchout.startsWith("]]") && insideDatei) {
                level--;
                if (level == 0) {
                    text = text.substring(2); //there was a problem with }} being left
                    watchout = StringExtensions.SafeSubstring(text, 0, 1);
                    insideDatei = false;
                }
            }

            if (level == 0) //if out of a template
                result.append(StringExtensions.SafeSubstring(watchout, 0, 1));

            //prepare next iteration
            text = StringExtensions.SafeSubstring(text, 1, text.length());
        }

        return result.toString();
    }

    private static String removeTemplates(String text, String startSeq, String finishSeq) {
        /* UNIT TESTS
        String res = removeTemplates("hi there{{. .{{..sad.}}. .}}","{{", "}}");
            String res1 = removeTemplates("hi there{{. .{{..sad.}}. .}}1","{{", "}}");
            String res2 = removeTemplates("hi there{{. .{{..sad.}}. .}}23","{{", "}}");
            String result = removeTemplates("hi there{{. .{{..sad.}}. .}} all right","{{", "}}");
            String result2 = removeTemplates("hi there{{...}} {{...}} {{...}} all right","{{", "}}");
            String result3 = removeTemplates("hi there{{.{|.|}..}} {{...}} {{...}} all right","{{", "}}");
        */

        StringBuilder result = new StringBuilder();
        boolean insideDatei = false;
        int level = 0;
        while (!text.equals("")) {

            //speed up search
            if (!insideDatei) {
                int beginKey = text.indexOf(startSeq);
                if (beginKey == -1) {//no more findings till the end
                    result.append(text); //copy what's left
                    return result.toString(); //and exit
                }
                result.append(text.substring(0, beginKey));
                text = text.substring(beginKey);
            }


            String watchout = text;

            if (watchout.startsWith(startSeq)) {
                level++;
                insideDatei = true;
            } else if (insideDatei && watchout.startsWith(startSeq)) { //for nested templates, which are indeed possible, though probably really seldom
                level++;
            }

            if (watchout.startsWith(finishSeq)) {
                level--;
                if (level == 0) {
                    text = text.substring(2); //there was a problem with }} being left
                    watchout = StringExtensions.SafeSubstring(text, 0, 1);
                    insideDatei = false;
                }
            }

            if (level == 0) //if out of a template
                result.append(StringExtensions.SafeSubstring(watchout, 0, 1));

            //prepare next iteration
            text = StringExtensions.SafeSubstring(text, 1, text.length());
        }

        return result.toString();
    }

    public static String CleanWikipediaArticleResponse(String text) {
        try {
            String EXTRACT = "<extract xml:space=\"preserve\">";
            if (text.contains(EXTRACT)) {
                int pos = text.indexOf(EXTRACT);
                //delete opening tag
                text = text.substring(pos);
                text = text.replaceFirst(EXTRACT, "");

                //delete closing tag
                text = text.substring(0, text.indexOf("</extract>"));

                text = text.replace("&quot;","\"");
                return text;
            } else {
                return "Article Not Found";
            }
        } catch (Exception ex) {
            return "Error on CleanWikipediaArticleResponse()";
        }
    }

    public static String GetRandomWikipediaArticleName() {
        try {
            //String content = new ReadTask().execute("http://de.wikipedia.org/w/api.php?format=xml&action=query&titles=" + name + "&prop=extracts&explaintext&exsectionformat=plain&exintro").get();
            String article_name = new HttpGetter().execute("http://de.wikipedia.org/w/api.php?action=query&format=xml&list=random&rnnamespace=0&rnlimit=1").get();

            DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();

            xmlFact.setNamespaceAware(false);

            DocumentBuilder builder = xmlFact.newDocumentBuilder();

            XPathFactory xpathFact = XPathFactory.newInstance();
            XPath xpath = xpathFact.newXPath();
            XPathExpression xPathExpression = xpath.compile("//random/page/@title");
            Document doc = builder.parse(new InputSource(new StringReader(article_name)));
            String imageUrl = xPathExpression.evaluate(doc, XPathConstants.STRING).toString();

            return imageUrl;

            //article_name = article_name.substring(article_name.indexOf("title=\"")+7);
            //int end = article_name.lastIndexOf("/></random>");
            //article_name = article_name.substring(0, end);
            //return article_name;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            //Toast.makeText(getApplicationContext(), "Exception: GetRandomWikiArticle()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }
        return "Error while getting WikiRandomArticleName";

    }

    public static String GetRandomStupidediaArticleName() {
        try {
            String article_name = new HttpGetter().execute("http://www.stupidedia.org/api.php?action=query&format=xml&list=random&rnnamespace=0&rnlimit=1").get();

            DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();

            xmlFact.setNamespaceAware(false);

            DocumentBuilder builder = xmlFact.newDocumentBuilder();

            XPathFactory xpathFact = XPathFactory.newInstance();
            XPath xpath = xpathFact.newXPath();
            XPathExpression xPathExpression = xpath.compile("//random/page/@title");
            Document doc = builder.parse(new InputSource(new StringReader(article_name)));
            String imageUrl = xPathExpression.evaluate(doc, XPathConstants.STRING).toString();

            return imageUrl;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            //Toast.makeText(getApplicationContext(), "Exception: GetRandomWikiArticle()-" + ex.toString() + "-" + ex.getStackTrace().toString(), Toast.LENGTH_LONG).show();
        }
        return "Error while getting WikiRandomArticleName";

    }
}

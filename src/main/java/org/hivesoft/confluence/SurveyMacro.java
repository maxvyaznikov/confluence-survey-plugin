/**
 * Copyright (c) 2006-2013, Confluence Community
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hivesoft.confluence;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.user.User;
import com.opensymphony.util.TextUtils;

/**
 * This very simple macro shows you the very basic use-case of displaying *something* on the Confluence page where it is used.
 * Use this example macro to toy around, and then quickly move on to the next example - this macro doesn't
 * really show you all the fun stuff you can do with Confluence.
 */
public class SurveyMacro extends BaseMacro
{

    // We just have to define the variables and the setters, then Spring injects the correct objects for us to use. Simple and efficient.
    // You just need to know *what* you want to inject and use.

    protected final PageManager pageManager;
    protected final SpaceManager spaceManager;

    public SurveyMacro(PageManager pageManager, SpaceManager spaceManager)
    {
        this.pageManager = pageManager;
        this.spaceManager = spaceManager;
    }

    public boolean isInline()
    {
        return false;
    }

    public boolean hasBody()
    {
        return false;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.NO_RENDER;
    }

    /**
     * This method returns XHTML to be displayed on the page that uses this macro
     * we just do random stuff here, trying to show how you can access the most basic
     * managers and model objects. No emphasis is put on beauty of code nor on
     * doing actually useful things :-)
     */
    public String execute(Map params, String body, RenderContext renderContext)
            throws MacroException
    {

        // in this most simple example, we build the result in memory, appending HTML code to it at will.
        // this is something you absolutely don't want to do once you start writing plugins for real. Refer
        // to the next example for better ways to render content.
        StringBuffer result = new StringBuffer();

        // get the currently logged in user and display his name
        User user = AuthenticatedUserThreadLocal.getUser();
        if (user != null)
        {
            String greeting = "Hello " + TextUtils.htmlEncode(user.getFullName()) + "<br><br>";
            result.append(greeting);
        }

        //get the pages added in the last 55 days to the DS space ("Demo Space"), and display them
        List list = pageManager.getRecentlyAddedPages(55, "DS");
        result.append("Some stats for the Demo space: <br> ");
        for (Iterator i = list.iterator(); i.hasNext();)
        {
            Page page = (Page) i.next();
            int numberOfChildren = page.getChildren().size();
            String pageWithChildren = "Page " + TextUtils.htmlEncode(page.getTitle()) + " has " + numberOfChildren + " children <br> ";
            result.append(pageWithChildren);
        }

        // and show the number of all spaces in this installation.
        String spaces = "<br>Altogether, this installation has " + spaceManager.getAllSpaces().size() + " spaces. <br>";
        result.append(spaces);

        // this concludes our little demo. Now you should understand the basics of code injection use in Confluence, and how
        // to get a really simple macro running.

        return result.toString();
    }

}
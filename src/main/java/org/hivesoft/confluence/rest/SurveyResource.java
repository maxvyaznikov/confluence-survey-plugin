package org.hivesoft.confluence.rest;

import au.com.bytecode.opencsv.CSVWriter;
import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.content.render.xhtml.XhtmlException;
import com.atlassian.confluence.core.ContentPropertyManager;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentDataNotFoundException;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.MacroDefinitionHandler;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.extras.common.log.Logger;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.sal.api.user.UserManager;
import org.hivesoft.confluence.macros.survey.SurveyMacro;
import org.hivesoft.confluence.macros.survey.model.Survey;
import org.hivesoft.confluence.macros.utils.PermissionEvaluator;
import org.hivesoft.confluence.macros.utils.SurveyUtils;
import org.hivesoft.confluence.rest.representations.CSVExportRepresentation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/pages/{pageId}/surveys")
public class SurveyResource {
  private static final Logger.Log LOG = Logger.getInstance(SurveyResource.class);

  protected final PageManager pageManager;
  protected final ContentPropertyManager contentPropertyManager;
  protected final PermissionEvaluator permissionEvaluator;
  protected final XhtmlContent xhtmlContent;
  //protected final AttachmentManager attachmentManager;

  public SurveyResource(PageManager pageManager, ContentPropertyManager contentPropertyManager, UserAccessor userAccessor, UserManager userManager, XhtmlContent xhtmlContent) {
    this.pageManager = pageManager;
    this.contentPropertyManager = contentPropertyManager;
    this.permissionEvaluator = new PermissionEvaluator(userAccessor, userManager);
    this.xhtmlContent = xhtmlContent;
    //this.attachmentManager = attachmentManager;
  }

  @GET
  @Path("/{surveyTitle}/export")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCSVExportForSurvey(@PathParam("pageId") long pageId, @PathParam("surveyTitle") String inSurveyTitle) {

    try {
      final String surveyTitle = URLDecoder.decode(inSurveyTitle, "UTF-8");
      final Page page = pageManager.getPage(pageId);

      if (page == null) {
        return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
      }
      LOG.info("Found page with id=" + pageId + " and title=" + page.getTitle());

      page.addAttachment(new Attachment());

      final List<Survey> surveys = new ArrayList<Survey>();
      xhtmlContent.handleMacroDefinitions(page.getBodyAsString(), new DefaultConversionContext(page.getContentEntityObject().toPageContext()), new MacroDefinitionHandler() {
        @Override
        public void handle(MacroDefinition macroDefinition) {
          if (SurveyMacro.SURVEY_MACRO.equals(macroDefinition.getName())) {
            final Map<String, String> parameters = macroDefinition.getParameters();
            String currentTitle = null;
            try {
              currentTitle = SurveyUtils.getTitleInMacroParameters(parameters);
            } catch (MacroException e) {
              e.printStackTrace();
            }
            LOG.info("surveyTitle for export=" + surveyTitle + ", currentTitle to check is=" + currentTitle);
            if (surveyTitle.equalsIgnoreCase(currentTitle)) {
              LOG.info("Try to reconstruct Survey...");
              surveys.add(SurveyUtils.createSurvey(macroDefinition.getBodyText(), page.getContentEntityObject(), macroDefinition.getParameters().get(SurveyMacro.KEY_CHOICES), contentPropertyManager));
              //surveys.add(SurveyUtils.reconstructBallot(ballotTitle, macroDefinition.getBodyText(), page.getContentEntityObject(), contentPropertyManager));
            }
          }
        }
      });

      if (!surveys.isEmpty()) {
        Calendar currentDate = new GregorianCalendar();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'hhmmss");
        final String fileName = "test.csv"; // surveyTitle + "-summary-" + simpleDateFormat.format(currentDate.getTime()) + ".csv";

        final StringWriter csvStringWriter = new StringWriter();
        CSVWriter writer = new CSVWriter(csvStringWriter, ';');
        // feed in your array (or convert your data to an array)
        String[] entries = "first#second#third".split("#");
        writer.writeNext(entries);
        writer.close();
        final byte[] csvBytes = "firstThird".getBytes(); //"UTF-8"); //csvStringWriter.toString().getBytes("UTF-8");
        LOG.info("csv has been created: " + csvBytes);
/*
        final List<Attachment> attachments = page.getLatestVersionsOfAttachments();
        for (Attachment someAttachment : attachments) {
          Attachment newAttachment = new Attachment();
          newAttachment.setFileName(someAttachment.getFileName() + "new");
          newAttachment.setContentType(someAttachment.getContentType());
          newAttachment.setVersion(1);
          newAttachment.setFileSize(someAttachment.getFileSize());
          newAttachment.setComment(someAttachment.getComment());
          newAttachment.setContent(page);
          page.addAttachment(newAttachment);

          pageManager.getAttachmentManager().saveAttachment(newAttachment, null, pageManager.getAttachmentManager().getAttachmentDao().getAttachmentData(someAttachment));
        }*/


        //Attachment attachment = new Attachment(fileName, "text/plain", csvBytes.length, "export of survey with title: " + surveyTitle);
        Attachment attachment = new Attachment();
        attachment.setFileName("moo.csv");
        attachment.setContentType("text/plain");
        attachment.setVersion(1);
        attachment.setFileSize(csvBytes.length);
        attachment.setContent(page);
        LOG.info("Attachment object created:" + attachment);

        page.addAttachment(attachment);
        LOG.info("attachment added to page");
        //pageManager.getAttachmentManager().getAttachmentDao().saveNewAttachment(attachment, new ByteArrayInputStream(csvBytes));
        //attachmentManager.getAttachmentDao().saveNewAttachment(attachment, new ByteArrayInputStream(csvBytes));
        pageManager.getAttachmentManager().saveAttachment(attachment, null, new ByteArrayInputStream(csvBytes));
        //attachmentManager.setAttachmentData(attachment, new ByteArrayInputStream(csvBytes));
        LOG.info("attachmentData has been set.");

        //final String attachmentUrlPath = page.getAttachmentUrlPath(attachments.get(attachments.size() - 1));
        final String attachmentUrlPath = page.getAttachmentUrlPath(attachment);
        LOG.info("returning path to attachment: " + attachmentUrlPath);

        final CSVExportRepresentation csvExportRepresentation = new CSVExportRepresentation();
        csvExportRepresentation.setUri(attachmentUrlPath);
        return Response.ok(csvExportRepresentation).build();
      }
      return Response.status(Response.Status.BAD_REQUEST).entity("Could not find the specified survey macro on the specified page!").build();
    } catch (XhtmlException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Response.status(Response.Status.BAD_REQUEST).entity("Encountered an unexpected problem!").build();
  }
}

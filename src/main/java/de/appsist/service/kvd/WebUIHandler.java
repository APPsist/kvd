package de.appsist.service.kvd;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import de.appsist.commons.lang.LangUtil;
import de.appsist.commons.lang.StringBundle;

/**
 * Handler for web ui requets.
 * @author simon.schwantzer(at)im-c.de
 */
public class WebUIHandler {
	private static final Logger logger = LoggerFactory.getLogger(WebUIHandler.class);
	
	private final String basePath;
	private Vertx vertx;
	private Map<String, Template> templates; // Map with handlebars templates for HTML responses. 
	
	private String currentUser;
	private Map<String, String> userImages;
	
	/**
	 * Creates the handler.
	 * @param basePath Base path for http requests. Is used templates are rendered.
	 * @param localFileHandler Handler to retrieve content list from.
	 * @param logger Logger for system information.
	 */
	public WebUIHandler(Vertx vertx, String basePath) {
		this.vertx = vertx;
		this.basePath = basePath;
		templates = new HashMap<>();
		try {
			TemplateLoader loader = new ClassPathTemplateLoader();
			loader.setPrefix("/templates");
			loader.setSuffix(".html");
			Handlebars handlebars = new Handlebars(loader);
			templates.put("learning", handlebars.compile("learning"));
			templates.put("modules", handlebars.compile("modules"));
			templates.put("process", handlebars.compile("process"));
			
		} catch (IOException e) {
			logger.fatal("Failed to load templates.", e);
		}
		
		currentUser = "defaultUser";
		userImages = new HashMap<>();
		String userImagesPath = "img/learning/background_user/";
		userImages.put("defaultUser", userImagesPath + "01.png");
	}
	
	/**
	 * Resolves a request for a static file. 
	 * @param response Response to send file.
	 * @param filePath Path of the file to deliver.
	 */
	public void resolveStaticFileRequest(HttpServerResponse response, String filePath) {
		response.sendFile(filePath);
	}
	
	public void resolveLearningRequest(HttpServerResponse response, String successId) {
		JsonObject data = new JsonObject();
		StringBundle bundle = LangUtil.getInstance(vertx.sharedData()).getBundle();

		data.putString("basePath", basePath);
		data.putString("title", bundle.getString("kvd.ui.title", "APPsist Wissensvisualisierungsdienst"));
		data.putString("background_user", userImages.get(currentUser));
		
		if (successId != null) {
			data.putString("successId", successId);
		}
		try {
			String html = templates.get("learning").apply(data.toMap());
			response.end(html);
		} catch (IOException e) {
			response.setStatusCode(500); 
			response.end("Failed to render template.");
		}
	}
	
	public void updateLearningView(String currentUser) {
		this.currentUser = currentUser;
	}
	
	public void resolveModulesRequest(HttpServerResponse response, String successId) {
		JsonObject data = new JsonObject();
		
		data=getModulesData(data);
		
		if (successId != null) {
			data.putString("successId", successId);
		}
		try {
			String html = templates.get("modules").apply(data.toMap());
			response.end(html);
		} catch (IOException e) {
			response.setStatusCode(500); 
			response.end("Failed to render template.");
		}
	}

	public void resolveProcessRequest(HttpServerResponse response, String successId) {
		JsonObject data = new JsonObject();

		
		data=getProcessData(data);
		
		if (successId != null) {
			data.putString("successId", successId);
		}
		try {
			String html = templates.get("process").apply(data.toMap());
			response.end(html);
		} catch (IOException e) {
			response.setStatusCode(500); 
			response.end("Failed to render template.");
		}
	}

	
	/*
	 * 
	 * Generation of content for webpage
	 * 
	 */
	
	
	
	private JsonObject getProcessData(JsonObject data) {
		// TODO Auto-generated method stub
		return new JsonObject();
	}
	
	private JsonObject getModulesData(JsonObject data) {
		data.putString("modfile", "img/modules/comm1.png");
		data.putString("tuerbild", "img/modules/tuer_ok.png");
		data.putString("federbild", "img/modules/tuer_ok.png");
		data.putString("deckelbild", "img/modules/tuer_ok.png");
		data.putString("bauteilbild", "img/modules/tuer_ok.png");
		return data;
	}
	
}

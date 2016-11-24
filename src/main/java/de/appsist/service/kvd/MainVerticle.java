package de.appsist.service.kvd;


import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

import de.appsist.commons.event.MachineStateChangedEvent;
import de.appsist.commons.misc.StatusSignalConfiguration;
import de.appsist.commons.misc.StatusSignalSender;
import de.appsist.commons.util.EventUtil;
import de.appsist.service.kvd.learning.LearningController;

/**
 * Main verticle for the machine state simulation service. Its a software endpoint simulation a connected machine to be used for testing and demonstration purposes. 
 * @author simon.schwantzer(at)im-c.de
 */
public class MainVerticle extends Verticle {
	private Logger logger = LoggerFactory.getLogger(MainVerticle.class);
	private JsonObject config;
	private LearningController learningController = new LearningController();
	private RouteMatcher routeMatcher;

	
	@Override
	public void start() {
		if (container.config() != null && container.config().size() > 0) {
			config = container.config();
		} else {
			logger.error("Warning: No configuration applied! Aborting.");
			System.exit(1);
		}
		
		initializeHTTPRouting();
		initializeEventBusHandler();
		
		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(routeMatcher);
		httpServer.listen(config.getObject("webserver").getInteger("port"));
		
		logger.info("APPsist \"Chancellor Visualisation Service \" has been started.");
		/*
		 * status signal
		 */
		
		JsonObject statusSignalObject = config.getObject("statusSignal");
		StatusSignalConfiguration statusSignalConfig;
		if (statusSignalObject != null) {
		  statusSignalConfig = new StatusSignalConfiguration(statusSignalObject);
		} else {
		  statusSignalConfig = new StatusSignalConfiguration();
		}

		StatusSignalSender statusSignalSender =
		  new StatusSignalSender("kvd", vertx, statusSignalConfig);
		statusSignalSender.start();
	}
	
	@Override
	public void stop() {
		logger.info("APPsist \"Chancellor Visualisation Service \" has been stopped.");
		
	}
	
	/**
	 * In this method the handlers for the event bus are initialized.
	 */
	private void initializeEventBusHandler() {

		//for Modules page
		
//	        vertx.eventBus().registerHandler("appsist:event:machinestateChangedEvent",
//		                new Handler<Message<JsonObject>>() {
//		            @Override
//				public void handle(Message<JsonObject> jsonMessage)
//		            {
//		                MachineStateChangedEvent msce =  (MachineStateChangedEvent) EventUtil
//		                        .parseEvent(jsonMessage.body().toMap());
//		                processMachineStateChangedEvent(msce);
//		            }
//		        });
		
	        
	        
	        
	        
		/*vertx.eventBus().registerHandler(ProcessCompleteEventHandler.ADDRESS, new ProcessCompleteEventHandler(config.getObject("processes"), stateController));	
		final SetMachineDataEventHandler setmachineDataEventHandler = new SetMachineDataEventHandler(vertx, midConfig.getInteger("port"), webserverConfig.getBoolean("secure"), midConfig.getString("basePath"));
		vertx.eventBus().registerHandler(SetMachineDataEventHandler.ADDRESS, setmachineDataEventHandler);
		vertx.eventBus().registerHandler("appsist:event:" + StartupCompleteEvent.MODEL_ID, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				logger.info("Received StartupCompleteEvent --> initializing RESTConnection");
				stateController.initializeRESTConnection();
				setmachineDataEventHandler.initializeRESTConnection();
			}
		});*/
	}
	
	protected void processMachineStateChangedEvent(MachineStateChangedEvent msce) {
		

		
		
	}

	/**
	 * In this method the HTTP API build using a route matcher.
	 */
	private void initializeHTTPRouting() {

		
		final String basePath = config.getObject("webserver").getString("basePath", "");
		routeMatcher = new BasePathRouteMatcher(basePath);
		final WebUIHandler webUiHandler = new WebUIHandler(vertx, basePath);
		
		routeMatcher.get("/learning", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				String successId = request.params().get("success");
				webUiHandler.resolveLearningRequest(request.response(), successId);
			}
		});
		
		routeMatcher.get("/modules", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				String successId = request.params().get("success");
				webUiHandler.resolveModulesRequest(request.response(), successId);
			}
		});
		
		routeMatcher.get("/process", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				String successId = request.params().get("success");
				webUiHandler.resolveProcessRequest(request.response(), successId);
			}
		});
		
		
		
		routeMatcher.getWithRegEx("/.+", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				request.response().sendFile("www" + request.path().substring(basePath.length()));
				//request.response().end("requested URL '" + request.path() + "' not found.");
			}
		});
	}
}

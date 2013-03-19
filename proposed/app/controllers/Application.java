package controllers;

import java.util.ArrayList;
import java.util.List;

import play.*;

import play.core.Router.Routes;
import play.libs.F.*;
import play.libs.*;
import play.mvc.*;
import play.data.*;

import views.html.*;
import models.*;

@Security.Authenticated(Secured.class)
public class Application extends Controller {

	static private Form<Source> sourceForm = Form.form(Source.class);
  
  public static Result home() {
    return ok(homePage.render());
  }
  
  public static Result search() {
  	User currentUser = Secured.getCurrentUser();
    return TODO;
  }
  
  public static Result streams() {
  	User currentUser = Secured.getCurrentUser();
    return ok(streamsPage.render(currentUser.streamList));
  }
  
  public static Result attachFunction() {
  	User currentUser = Secured.getCurrentUser();
    return ok(attachFunctionPage.render(currentUser.sourceList));
  }

  public static Result manage() {
  	User currentUser = Secured.getCurrentUser();
    return ok(managePage.render(currentUser.sourceList, sourceForm));
  }
  
  // -- Javascript routing
  public static Result javascriptRoutes() {
      response().setContentType("text/javascript");
      return ok(
      		play.Routes.javascriptRouter("jsRoutes",
          		controllers.routes.javascript.Application.home(),
          		controllers.routes.javascript.CtrlSource.deleteParser(),
          		controllers.routes.javascript.CtrlSource.addParser(),
          		controllers.routes.javascript.CtrlStream.delete(),
          		controllers.routes.javascript.CtrlStream.clear(),
          		controllers.routes.javascript.CtrlStream.deleteByKey(),
          		controllers.routes.javascript.CtrlStream.clearByKey(),
          		controllers.routes.javascript.CtrlStream.setPublicAccess(),
          		controllers.routes.javascript.CtrlStream.setPublicSearch(),
          		controllers.routes.javascript.CtrlUser.followStream()
          )
      );
  }
    
}

/*
 * Copyright (c) 2013, Swedish Institute of Computer Science
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of The Swedish Institute of Computer Science nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SWEDISH INSTITUTE OF COMPUTER SCIENCE BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

/* Description:
 * TODO:
 * */

/*
copyright (c) 2013, swedish institute of computer science
all rights reserved.

redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

this software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. in no event shall <copyright holder> be liable for any
direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and
on any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/
package controllers;

import models.User;
import play.mvc.Http.Context;
import play.mvc.Http.Request;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.mvc.Security;
import views.html.loginPage;

public class Secured extends Security.Authenticator {

	@Override
	public String getUsername(Context ctx) {
		String name = getUsername(ctx.session());
		return (name != null) ? name : getUsername(ctx.request());
	}
	
	public static String getUsername(Session session) {
		Long id = getUserId(session);
		if(id != null){
			return User.get(id).getUserName();
		}
		return null;
	}
	
	public static String getUsername(Request request) {
		Long id = getUserId(request);
		if(id != null){
			return User.get(id).getUserName();
		}
		return null;
	}

	@Override
	public Result onUnauthorized(Context ctx) {
		return ok(loginPage.render(""));
	}

	public static User getCurrentUser() {
		return User.get(Secured.getUserId(Context.current()));
	}
	
	public static String getUsername() {
		return new Secured().getUsername(Context.current());
	}
	
	public static Long getUserId(Context ctx) {
		Long id = getUserId(ctx.session());
		if(id == null){
			id = getUserId(ctx.request());
		}
		return id;
	}
	
	public static Long getUserId(Session session) {
		String id = session.get("id");
		try{
		if (id != null && User.exists(Long.parseLong(id)))
			return Long.parseLong(id);
		else
			return null;
		} catch(Exception e) {
			return null;
		}
	}
	
	public static Long getUserId(Request request) {
		String userToken = request.getQueryString("user_token");
		User current = User.getByToken(userToken);
		if ( current != null ) {
			return current.getId();
		}	else
			return null;
	}
}

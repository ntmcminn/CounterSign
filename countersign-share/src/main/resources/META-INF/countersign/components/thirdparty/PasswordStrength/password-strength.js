/**
 * password_strength_plugin.js
 * Copyright (c) 2010 myPocket technologies (www.mypocket-technologies.com)


 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 * @author Darren Mason (djmason9@gmail.com), modified by Nathan McMinn (nmcminn@gmail.com)
 * @date 3/13/2009
 * @projectDescription Password Strength Meter is a jQuery plug-in provide you smart algorithm to detect a password strength. Based on Firas Kassem orginal plugin - http://phiras.wordpress.com/2007/04/08/password-strength-meter-a-jquery-plugin/
 * @version 1.0.1
 * 
 * @requires jquery.js (tested with 1.3.2)
 * @param shortPass:    "shortPass",    //optional
 * @param badPass:      "badPass",      //optional
 * @param goodPass:     "goodPass",     //optional
 * @param strongPass:   "strongPass",   //optional
 * @param noPass:		"noPass", 		//optional
 * @param baseStyle:    "testresult",   //optional
 * @param confirm:		true,
 * @param minLength		6
 * @param goodScore:    34,
 * @param strongScore:  68,
 * @param userid:       "",             //required override
 * @param messageloc:   1               //before == 0 or after == 1
 * 
 */

(function($){ 
	$.fn.shortPass = 'Too short';
	$.fn.badPass = 'Strength: Weak';
	$.fn.goodPass = 'Strength: Good';
	$.fn.strongPass = 'Strength: Strong';
	$.fn.samePassword = 'Username and Password identical.';
	$.fn.passDoesNotMatch = 'Passwords do not match.';
	$.fn.noPass = 'Please provide a password';
	$.fn.resultStyle = "";

	$.fn.passStrength = function(options) {  

		var defaults = {
				shortPass:              "shortPass",    //optional
				badPass:                "badPass",      //optional
				goodPass:               "goodPass",     //optional
				strongPass:             "strongPass",   //optional
				noPass:					"noPass",		//optional
				baseStyle:              "testresult",   //optional
				userid:                 "",             //required override
				messageloc:             1,				//before == 0 or after == 1
				confirm:				true,
				minLength:				6,
				goodScore:				34,
				strongScore:			68,
				onChangeCallback:		function(){}
		}; 
		var opts = $.extend(defaults, options);  

		return this.each(function() { 
			
			// only show the confirm field if it is required
			if(!options.confirm)
			{
				$("#" + this.id + "-confirmpasswordcomponent").hide();
			}
			
			var passField = $("#" + this.id + "-password");
			var confirmField = $("#" + this.id + "-confirmpassword");
			var hiddenPasswordField = $(this);

			//FUNCTIONS
			$.fn.keyuphandler = function(event)
			{
				// if we don't need to confirm, then bypass all of this testing
				if(opts.confirm)
				{
					var results = $.fn.teststrength($(passField).val(),$(confirmField).val(),$(opts.userid).val(),opts);
	
					if(opts.messageloc === 1)
					{
						$(confirmField).next("." + opts.baseStyle).remove();
						$(confirmField).after("<span class=\""+opts.baseStyle+"\"><span></span></span>");
						$(confirmField).next("." + opts.baseStyle).addClass($(confirmField).resultStyle).find("span").text(results);
					}
					else
					{
						$(confirmField).prev("." + opts.baseStyle).remove();
						$(confirmField).before("<span class=\""+opts.baseStyle+"\"><span></span></span>");
						$(confirmField).prev("." + opts.baseStyle).addClass($(confirmField).resultStyle).find("span").text(results);
					}
				}
				else
				{
					$(hiddenPasswordField).val($(passField).val());
				}
				
				CounterSign.util.JQuery.FormHelper.updateSubmitElements();
			};

			$.fn.teststrength = function(password,confirm,username,option){
				var score = 0; 

				//password not provided
				if (password.length == 0){
					this.resultStyle = option.noPass;
					$(hiddenPasswordField).val("");
					return $(this).noPass;
				}
				
				//password < 4
				if (password.length < option.minLength) { 
					this.resultStyle =  option.shortPass;
					$(hiddenPasswordField).val("");
					return $(this).shortPass; 
				}

				//password == user name, if the username is provided
				if (username && password.toLowerCase()==username.toLowerCase()){
					this.resultStyle = option.badPass;
					$(hiddenPasswordField).val("");
					return $(this).samePassword;
				}

				// check and see if the password and confim pass match.
				if(option.confirm && !(confirm === password)){
					this.resultStyle = option.badPass;
					$(hiddenPasswordField).val("");
					return $(this).passDoesNotMatch;
				} 
				
				//password length
				score += password.length * 4;
				score += ( $.fn.checkRepetition(1,password).length - password.length ) * 1;
				score += ( $.fn.checkRepetition(2,password).length - password.length ) * 1;
				score += ( $.fn.checkRepetition(3,password).length - password.length ) * 1;
				score += ( $.fn.checkRepetition(4,password).length - password.length ) * 1;

				//password has 3 numbers
				if (password.match(/(.*[0-9].*[0-9].*[0-9])/)){ score += 5;} 

				//password has 2 symbols
				if (password.match(/(.*[!,@,#,$,%,^,&,*,?,_,~].*[!,@,#,$,%,^,&,*,?,_,~])/)){ score += 5 ;}

				//password has Upper and Lower chars
				if (password.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/)){  score += 10;} 

				//password has number and chars
				if (password.match(/([a-zA-Z])/) && password.match(/([0-9])/)){  score += 15;} 
				//
				//password has number and symbol
				if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([0-9])/)){  score += 15;} 

				//password has char and symbol
				if (password.match(/([!,@,#,$,%,^,&,*,?,_,~])/) && password.match(/([a-zA-Z])/)){score += 15;}

				//password is just a numbers or chars
				if (password.match(/^\w+$/) || password.match(/^\d+$/) ){ score -= 10;}

				//verifying 0 < score < 100
				if ( score < 0 ){score = 0;} 
				if ( score > 100 ){  score = 100;} 

				if (score < option.goodScore){ this.resultStyle = option.badPass; return $(this).badPass;}

				// if the score pass the "badpass" tests, go ahead and set
				// the value of the hidden element.
				$(hiddenPasswordField).val(password);
				
				if (score < option.strongScore){ this.resultStyle = option.goodPass;return $(this).goodPass;}

				this.resultStyle= option.strongPass;
				return $(this).strongPass;

			};

			$(passField).unbind().keyup($.fn.keyuphandler);
			$(confirmField).unbind().keyup($.fn.keyuphandler);
			
			// now go ahead and call the keyuphandler, just to be sure that the 
			// initial state is correct
			$.fn.keyuphandler();
		});  
	};  
})(jQuery); 


$.fn.checkRepetition = function(pLen,str) {
	var res = "";
	for (var i=0; i<str.length ; i++ ) 
	{
		var repeated=true;

		for (var j=0;j < pLen && (j+i+pLen) < str.length;j++){
			repeated=repeated && (str.charAt(j+i)==str.charAt(j+i+pLen));
		}
		if (j<pLen){repeated=false;}
		if (repeated) {
			i+=pLen-1;
			repeated=false;
		}
		else {
			res+=str.charAt(i);
		}
	}
	return res;
};



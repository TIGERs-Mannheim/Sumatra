/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2010
 * Author(s): BernhardP, AndreR
 * *********************************************************
 */
package edu.tigers.sumatra;

import java.awt.EventQueue;

import edu.tigers.sumatra.log.JULLoggingBridge;


/**
 * Go a step back and look at christmas tigger
 * 
 * <pre>
 *                                                            `~
 *                                                            `**;
 *                                                            i+o!.
 *                                                       `   ^*i?$;
 *                                                      :oo=-*%$O*
 *                                                      _**!O$OO#*i
 *                                                      ~$$$O#%?$#&*
 *                                                  ^;oeeoo%$O$e%!&&`
 *                                                _*$%#%?iio##$?!?#&-
 *                                              `o+:+!O&O?**%&O%%?$&~
 *                                              ??i=io!##?ee!&#$?+=%
 *                                             =$&#?**e%#!**e&ee.:;++
 *                                            `!*!$%eeee?*e*=_e.~_;==i;:;+=:
 *                                            ;;+o*ee**!*;^  ~*~.-:;+=;-;e?e
 *                                            o:ie!!**o.     ;=~.-_:;:_e$?*o
 *                                           .i=?&@&$*       _=;.~-_-;$$?%e_
 *                                           ;;=!O&&O.       `ii~~-~+O$!*+e~
 *                                     `~^   =:=*e!#?         =;;~~i%?!%-.++
 *                                    :o:e;  *eOO?e*=         _++~=$?!*?__=:
 *                       ~_          ^+!ee=~ $#&##e*;         `o-:%?eoieoio
 *                      o=+*-        __;**i_ %$$O%e*;          *_!?!o=o; ^
 *                   `;ooo*eeo^      .+e==e. ?%?*ee*+          ii%!o=*:
 *                 ^=eo*eeeeee!;      o=;i+  ?eioee*o         .=?eoi*~
 *                +*o*ee***ee!!?!: `;*e-_.   !;i*$#??`        ;=!o*i`
 *              ^oi*ee****ee!????%%%e?-      i;*O&&&Oi        ~*e=.
 *             `*=*ee****ee!?%%%%%%%$+       -o#&&#O!e?:`
 *             o+*ee****ee!?%?%??%?%i         %&&&Oee$&&#ei_`
 *            =ie%$$$%??!!?%?i   `..          +##&**!&##?oo*!eo~
 *           :$e=:---_;=e$O%?^ `               %$?oo?&#?eeee*%#$%+^
 *         _*:^`      ^`^^:?o`oo*+             ^Oiio?&$ee!O%!&#%*o!i`
 *        .=.^`      !#e-.~~=*iiio*`            ~e+ie$e*!&#e%&?e!$O&$~
 *        =..`  ^~`^i&&&O*=;+!***ee:             ~!==oo*##$*%$e!OO#O$O-
 *        =..` .$&?o*e$&&#%OO!eee_-;              ^o*i=e#&?o!e*!O&&&&O$`
 *       ^=-^``?&&o~~~-:*OOe%$e!=;_;                ^:o%#O*=iii%&&###&&o
 *        +=.`;&&*~~~-o=-:?!*!e!o;;-      ^`            ^_=*ei=O&###$%??`
 *         =_^?@$--~.~~:_-_!**eO*oi     `~                  `:!OO#$!ee*o_
 *         +_.#&+o_~.~~~~--*o?!Oe-     -~                      +$*o*eeeoi
 *         :~+&$-+~~..-i---eoe$%i     :^                        o;=oeee*e
 *         +.?#o--~~~.;O_-:eo*O$?   `-      `^`                 .ii%$$?*e`
 *         ;=$$+--~=e~~eoe*ooe?_o  ..   ^__-^                    ?#&##O%e.
 *     `.`  *O!*_--=$_*!e!e*?O=o= ~^ `:;~                        ?O&&&&O%~
 *    .oioo;=Oo!i---*!*!:.^^~e_.=~`^:_                           %#&&&&#?.
 *    =;=iooeeoo!*_**ei^^^^^.-*~_+~.                             %$#&##&#^
 *    o+io*ee**eoe?*e=^^^^^.~-e-~=                              .!=o!$#&$`
 *    !!?%%!eee%****e^^^...~-_e_~=__:__-~~.^`      .+ii:^   ^+!$$ee!eee%$
 *    i-_:=?OO!O??$$_....~~-__?:-+               `*e*?O#Oe+oo+i!O%=io****
 *    ;:-:oe!?#$!o;*~~~~---__:?:-=              `%&#?o*!$O&&%i+=iee*****o
 *     o--_io !$_i~*-________*o:-=:.` ``        e#&&&#e%#$&&##*ooe!?!e**e;
 *     ^ioo;  ^%+i-i+-______;%+:-!oe_e*+       :%$##&&OO$#&&&O?e$###&#?**e-
 *             -!o-_eo---_-:%i+_+e*e*oe*o      !e?$#&O+;o%&&#$$?i++=*%O%**e`
 *           ~-~-*-_;*?*i=*?i+;_??!$e%?e=     .e=**!&!;:;+*O##O?%%%ei+ie**oi
 *        .--   ~;=-:+=io*i==+:*OO%$O$O%=`_   +$$%%O?O&!+=io$#$$OO$$O%io***e~
 *     `~.    `-  ;i_:+====++:o?##!ieOO%*eo+` i&&#*eOOO&%iio*#####O$O#$****oo
 *    ^^     .-  `.-e+::;;;;;e$i$$*e!O!i*?o*__!&$i__+?&&&eooeOO##O$%%#O%e*e*?
 *          _.   _  o??eoo*!O##=e#??%$eo!%?*$#%$O%!=+=e#@#**e##?o===i*?!!*e*e-
 *         ;`   :  -+i*#&######oi##$O%*??%$!$#&&&&#$eii*##?e!Oe++===ioo**ee**:
 *       `_    :` _e?o=O#####OOe;$OOOO%$$OO%?%?$&&&#O*ooe#%e!%e??eoioo***ee**:
 *      ^-    ._  +!%*+%###OOOO!:e%%*OO$O$?i*?o?O&&&#Ooo*?Oee##&&&#$!****ee**-
 *     .~     :   *$*e;*OOOOO$$!_=$!?!#OOeo!e?oi*O&#&&?*ee$eeO##O$$O#$e*eee*!^
 *    ^.     -`   `e*?:+O$$$$$$i-:%%$$O*%%???%oeo*$#O&Oeeee!%*i*$O%%%ee*eeeo!
 *   `.      -   `i:!O::!$$$$$?--:?$$?!!!$$O$$o*!**$###!ee?!+;++i%e****eee*o+
 *   `      .    ooe%#=:;%$$$!_-_:!!%$!%%O#OO!?!e!eeO&&?e!!;;++==i%***eeeeoe`
 *          .    *e*%#e:__++:---_:%%%%$$O!*$$?e!o!!e!##?e%+;++===i*?**e****-
 * 				   ??$$$;:_-----_:i$%!$$#%ee%#%??*o?ee%#%e!**===iiioe*ee**e_
 *              -!%O%#O?;:___:::;%e%?O$#??!e$$$$?*o%eeO%!?!!%*iiiio**ee*e-
 *              .e?OOOO&%+:::::i%*!%$?e#%$%$OO#&#Oee%e?%?%ee!%!iooo**ee*e
 *             :*?$$?e!O##?*oe%?oio$%!?$$$O$OO##O?eo?%?!o%eeee%!ooo***e*e`
 * 				i$?$!?%%#$#$%?%io*?$!$%%OOO$$OO$!ee*o%+++!?%eee%eo***ee*e
 *            .*o%$?$$!%%o?&#%%???!!%O$$%e$?*$$!ee!$O#*+;=%?$%!!%****ee**
 *            _ee*$$?O$$*!%O$$$!?O%o$%%%o*!%e?%!!%##O$$;:_??%??e!?******=
 *            ;e!ee$$%$$e$?$$$%$%e*e$%%e!!e$%!$!!O&&#OO=_o~e!%%!e?e****!.
 *            +!?!ee%%$$%OO#O$?**e!!O%$%O??#%$!!$&&&###?i` `?eeeeee****o
 *            i??$$?ee%$%$#O?eee!!!$OO%#$%OO$$!$&&#O$$%e    ^?eeeee***!`
 *            e??O$$$?$ee!$?!!!!!??O$eeO$OO#$!!%?!!ee!?%     ^e!eee**!~
 *            ?%?%$$$#!**e$%$$%?!%$%?%!O$$%%$!e*eeee?##O~      +?!*ee~
 *            $$%?%%$#%!!?OO$$O$!%O$%%!OOO%$!e***ee?O&OO;        -:-
 *           ^$$$$$$$O$$$$#$$$$??O#OO$OO$%%$?*oo**e%#&#Oo
 *           ~%%%$$$$OO%%%O%$%?%$O#O%%OO%$^`!*ooo*?$%?!ee
 *           -%???%$#$OOOO#$%%$$$$O??!!$$$^ :*iio**eeeee?`
 *           `%!!?OOOO$$OOOO%?%%%$#OO%!$!e:  eiiio*ee!$OO~`ioo=^
 *            _*e$O$$O%?O$$OO%???$#$$%$%!ei  _*=ii**?&#&&o*;;;;*_
 *              i?$$%$*%o$$$OO%%%$O##O%!!e!   *==io?&&###$+=iii=o-
 *             =e?%??! ..%%$$OO%%O##&##$!!O.  ~*=ieO#OO$Oo=ioo**o*^
 *           `ie!!!!?;  `???%$OO#$%O#&#?!%#+   i==i**ee!!iio**e**oi
 *          +ee!!!!!?    eeeee!??OO##O?ee!%o   ^*=iio*e%oio*eeeee*e
 *          ^;ii*!!?:    +***eee!!$?eeeeee*e`   +i=io*!eoo*eeeeee**
 *              ;!!*     ^!*ee;oeeO%eeeee**e~    e==i*!oo*eeeeeee*+
 *              =e!`      i!eo   ^?$#$%e****;    -o+ieoo*eeee!ee*e^
 *              oi`       `e!e;=+;i?##O!****i  .:_e=ioo*e!?%%eeee;
 *              `          ^?%:__:;=o*oo**e*o -*iiooo**e!%%%?e**o
 *                         _o?:_:;+=iooo**e*o ooo***eeeee!!ee*eo
 *                        `o_;;;;+=ioooo**e*+ ****eeeeeee**e?*-
 *                        _++++==ioooooo*e*!. _!*eeee***o=;-`
 *                        ;+iiiiooo*oio*eee+   :e!e+
 *                        _iioo***e?ooo*e!i
 *                         ooo**e!$ooo*e?+
 *                          ;ooi+:!o**ee^
 *                               .!*ee?.
 *                                o??!~
 *                                 ``
 * http://wchaverri.files.wordpress.com/2011/11/christmas-tigger-wreath.jpg
 * </pre>
 */
/**
 * <pre>
 * __ _-==-=_,-.
 * /--`' \_@-@.--<
 * `--'\ \ <___/. The wonderful thing about Tiggers,
 * \ \\ " / is Tiggers are wonderful things.
 * >=\\_/`< Their tops are made out of rubber,
 * ____ /= | \_/ their bottoms are made out of springs.
 * _' `\ _/=== \__/ They're bouncy, trouncy, flouncy, pouncy,
 * `___/ //\./=/~\====\ Fun, fun, fun, fun, fun.
 * \ // / | ===: But the most wonderful thing about Tiggers is,
 * | ._/_,__|_ ==: __ I'm the only one.
 * \/ \\ \\`--| / \\
 * | _ \\: /==:-\
 * `.__' `-____/ |--|==:
 * \ \ ===\ :==:`-'
 * _> \ ===\ /==/
 * /==\ | ===\__/--/
 * <=== \ / ====\ \\/
 * _`-- \/ === \/--'
 * | \ ==== |
 * -`------/`--' /
 * \___-'
 * The starter class of Sumatra.
 * Sumatra uses the MVP-passive view pattern in combination with moduli (a module-system for Java).
 * Make sure that you understand this approach to design an application,
 * before investigating Sumatra.
 * 
 * @author bernhard
 */
public final class Sumatra
{
	private Sumatra()
	{
	}
	
	
	static
	{
		// Connect java.util.logging (for jinput)
		JULLoggingBridge.install();
		
		// enable AWT keyboard for RCM
		// -Djinput.plugins=net.java.games.input.AWTEnvironmentPlugin
		// System.setProperty("jinput.plugins", "net.java.games.input.AWTEnvironmentPlugin");
	}
	
	
	/**
	 * Creates the model of the application and redirects to a presenter.
	 * 
	 * @param args
	 */
	public static void main(final String[] args)
	{
		EventQueue.invokeLater(() -> new MainPresenter());
	}
	
	
	/**
	 * the static part of this class should have been executed now
	 */
	public static void touch()
	{
	}
}

import java.awt.*;
import java.applet.*;
import java.util.*;

/**
 * An applet to demostrate central limit theorem of probability.
 * It utilizes an expression evaluator class by The-Son LAI.
 * <p>
 * The whole program is distributed under the Modified BSD
 * license with the kind permission from The-Son. See below.
 * <p>
 * Copyright (c) 2001 by Jarno Elonen and The-Son LAI
 * <p>
 * http://iki.fi/elonen/, http://lts.online.fr/java/
 */
public class CentralLimitApplet extends Applet
{
	/*
	 * The distribution licence
	 * 
	 * Redistribution and use in source and binary forms, with or without
	 * modification, are permitted provided that the following conditions
	 * are met:
	 * 
	 * Redistributions of source code must retain the above copyright notice,
	 * this list of conditions and the following disclaimer. Redistributions in
	 * binary form must reproduce the above copyright notice, this list of
	 * conditions and the following disclaimer in the documentation and/or other
	 * materials provided with the distribution. The name of the author may not
	 * be used to endorse or promote products derived from this software without
	 * specific prior written permission. 
	 *  
	 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
	 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	 */

	/**
	 * Start the applet, create panels
	 */
	public void init()
	{
		this.setLayout( null );
		
		// Create the parameter panel
		Panel p = new Panel();
		p.setLayout( new FlowLayout( FlowLayout.LEFT, 0,0 ));
		p.add( new Label( "Examine sum of", Label.LEFT ));
		p.add( sumOfTF );
		p.add( new Label( "occurences of a" ));
		p.add( new Label( "phenomenon" ));
		p.add( new Label( "whose min =" ));
		p.add( minTF );
		p.add( new Label( "," ));
		p.add( new Label( "max =" ));
		p.add( maxTF );
		p.add( new Label( "," ));
		p.add( new Label( "prob. func. P(x)=" ));
		p.add( funcTF );
		p.add( new Label( "and repeat it" ));
		p.add( repeatTF );
		p.add( new Label( "times.            " ));
		p.add( goButt );
		Panel p2 = new Panel( new BorderLayout());
		p2.add( BorderLayout.CENTER, p );
		Panel p3 = new Panel( new BorderLayout());
		p3.add( BorderLayout.CENTER, goButt );
		p2.add( BorderLayout.SOUTH, p3 );
		add( p2 );
		p2.setBounds( 0,0, 100, getSize().height );
		
		myImg = createImage( getSize().width-100, getSize().height );		
		clearCanvas();
		
		repaint();
	}

	/**
	 * Redraw the screen
	 */
	public void paint( Graphics g )
	{
		g.drawImage( myImg, 100,0, null );
	}

	/**
	 * Listen for button strokes
	 */
	public boolean action( Event evt, Object what )
	{
		if ( evt.target == goButt )
		{
			simulate();
			repaint();
			return true;
		}
		return false;
	}
	
	/**
	 * This can be called from Javascript.
	 * Sets the text fields and simulates a round.
	 */
	public void simulate( int min, int max, int sum,
						  int repeat, String func )
	{
		minTF.setText( "" + min );
		maxTF.setText( "" + max );
		sumOfTF.setText( "" + sum );
		repeatTF.setText( "" + repeat );
		funcTF.setText( func );
		simulate();
		repaint();
	}
	
	/**
	 * Paints the canvas white and draws
	 * black borders around it
	 */
	private void clearCanvas()
	{		
		int w = myImg.getWidth(null), h = myImg.getHeight(null);
		Graphics g = myImg.getGraphics();
		g.setColor( Color.white );
		g.fillRect( 0,0, 99999, 99999 );
		g.setColor( Color.black );
		g.drawRect( 0,0, w-1,h-1 );		
	}

	/**
	 * Runs a simulation with given parameters
	 * and draws the result as a histogram to the screen.
	 */
	private void simulate()
	{
		try
		{
			int min = getLimited(  minTF, "Min", -9999, 9999 );
			int max = getLimited(  maxTF, "Max", -9999, 9999 );
			int sumOf = getLimited(  sumOfTF, "Sum", 1, 1000 );
			int repeat = getLimited(  repeatTF, "Repeat count", 1, 10000000 );
			
			if ( min >= max )
				throw new Exception( "Min must be < Max." );
			
			int totalMin = (min*sumOf);
			int totalMax = (max*sumOf);		
		
			Graphics g = myImg.getGraphics();
			int w = myImg.getWidth(null), h = myImg.getHeight(null);
		
			clearCanvas();
			repaint();
		
			// Draw the scale
			g.setColor( Color.black );
			g.drawString( "" + totalMin, 2,h-8 );
			String maxStr = "" + totalMax;	
			g.drawString( maxStr, w-g.getFontMetrics().stringWidth( maxStr )-2, h-8 );
			String avgStr = "" + (totalMin+totalMax)/2;
			g.drawString( avgStr, w/2-g.getFontMetrics().stringWidth( avgStr )/2-2, h-8 );

			myEval.setExpression( funcTF.getText());
					
			int range = max-min+1;
			int totalRange = totalMax-totalMin+1;
		
			// Calculate propabilities from the function
			double fTotal = 0;
			double prob[] = new double[ range ];
			for ( int i=0; i<range; ++i )
			{
				myEval.addVariable( "x", i+min );
				Double dbl = myEval.getValue();
				
				if ( dbl == null )
					throw new Exception( "Invalid function '" + 
						funcTF.getText() + "'" );
				
				prob[i] = dbl.doubleValue();
				fTotal += prob[i];
			}
			// Normalize P to 0..1
			for ( int i=0; i<range; ++i )
				prob[i] /= fTotal;
		
			// Simulate the experiment
			double maxFreq = 0;
			double freq[] = new double[ totalRange  ];
			for ( int i=0; i<repeat; ++i )
			{
				int sum = 0;
				for ( int j=0; j<sumOf; ++j )
				{
					double d = myRnd.nextDouble();
					int k=0;
					while ( d>prob[k] && k<=range )
						d -= prob[k++];
					sum += k;
				}
				freq[sum]++;
				if ( freq[sum] > maxFreq )
					maxFreq = freq[sum];
			}
			// Normalize freq to 0...1
			for ( int i=0; i<totalRange; ++i )
				freq[i] /= maxFreq;
		
			// Draw the result histogram
			g.setColor( Color.blue );		
			for ( int x=1; x<w-1; ++x )
			{
				int v = (int)(freq[(int)(totalRange*(double)x/w)] * (h-60));
				g.drawLine( x,h-30, x,h-30-v );
			}
		}
		catch( Exception e )
		{
			clearCanvas();
			myImg.getGraphics().drawString( e.getMessage(), 10,20 );
		}
	}

	/**
	 * Gets an integer from given TextField or throw an Exception if
	 * it doesn't fall into given limits or is not a number.
	 */
	private int getLimited( TextField tf, String name, int min, int max )
		throws Exception
	{
		try
		{
			int val = Integer.parseInt( tf.getText());
			if ( val < min || val > max )
				throw new NumberFormatException();
			return val;
		}
		catch( NumberFormatException nfe )
		{
			String msg = "'" + name + "' must be an integer between [" +
						 min + ", " + max +"]";
			throw new Exception( msg );
		}
	}
	
	private TextField
		sumOfTF = new TextField( "10", 12 ),
		minTF = new TextField(  "1", 8 ),
		maxTF = new TextField(  "6", 8 ),
		funcTF = new TextField( "1/6", 12 ),
		repeatTF = new TextField( "10000", 12 );
	
	private Button goButt = new Button( "Simulate" );	
	private Image myImg;
	
	private MathEvaluator myEval = new MathEvaluator();
	private Random myRnd = new Random();
	
	
	/**
	 * <i>Mathematic expression evaluator.</i> Supports the following functions:
	 * +, -, *, /, ^, %, cos, sin, tan, acos, asin, atan, sqrt, sqr, log, min, max, ceil, floor, abs, neg, rndr.<br>
	 * When the getValue() is called, a Double object is returned. If it returns null, an error occured.<p>
	 * <pre>
	 * Sample:
	 * MathEvaluator m = new MathEvaluator("-5-6/(-2) + sqr(15+x)");
	 * m.addVariable("x", 15.1d);
	 * System.out.println( m.getValue() );
	 * </pre>
	 * Refactored slightly for smaller size by Jarno Elonen
	 * @version 1.1
	 * @author 	The-Son LAI, <a href="mailto:Lts@writeme.com">Lts@writeme.com</a>
	 * @date     April 2001
	 **/
	public static class MathEvaluator
	{
	   	protected static 	Operator[] 	operators 	= null;
		private 			Node 		node       	= null;
		private 			String  	expression 	= null;
	    private 			Hashtable 	variables  	= new Hashtable();

	    /***
	     * adds a variable and its value in the MathEvaluator
	     */
	    public void addVariable(String v, double val)
	    {
			variables.put(v, new Double(val));
	    }

	    /***
	     * sets the expression
	     */
	    public void setExpression(String s)
	    {
	       	if ( operators == null )
				initializeOperators();
			
		    expression = s;
	    }

	    /***
	     * evaluates and returns the value of the expression
	     */
	    public Double getValue()
	    {
	       	if (expression == null) return null;

	    	try
	        {
	            node = new Node(expression);
	            return evaluate(node);
	        }
	        catch (Exception e)
	        {
	        	e.printStackTrace();
	            return null;
	        }
	    }

	    private static Double evaluate(Node n)
	    {
	        if ( n.hasOperator() && n.hasChild() )
	        {
	            if ( n.nOperator.getType() == 1 )
	                n.nValue = evaluateExpression( n.nOperator, evaluate( n.nLeft ), null );
	            else if ( n.nOperator.getType() == 2 )
	                n.nValue = evaluateExpression( n.nOperator, evaluate( n.nLeft ), evaluate( n.nRight ) );
	        }
	        return n.nValue;
	    }

	    private static Double evaluateExpression(Operator o, Double f1, Double f2)
	    {
	        String op 	= o.getOperator();
	        Double res 	= null;

	        if  	 ( "+".equals(op) ) 	res = new Double( f1.doubleValue() + f2.doubleValue() );
	        else if  ( "-".equals(op) ) 	res = new Double( f1.doubleValue() - f2.doubleValue() );
	        else if  ( "*".equals(op) ) 	res = new Double( f1.doubleValue() * f2.doubleValue() );
	        else if  ( "/".equals(op) )  	res = new Double( f1.doubleValue() / f2.doubleValue() );
	        else if  ( "^".equals(op) )  	res = new Double( Math.pow(f1.doubleValue(), f2.doubleValue()) );
	        else if  ( "%".equals(op) )  	res = new Double( f1.doubleValue() % f2.doubleValue() );
	        else if  ( "&".equals(op) )  	res = new Double( f1.doubleValue() + f2.doubleValue() ); // todo
	        else if  ( "|".equals(op) )  	res = new Double( f1.doubleValue() + f2.doubleValue() ); // todo
	        else if  ( "cos".equals(op) )  	res = new Double( Math.cos(f1.doubleValue()) );
	        else if  ( "sin".equals(op) )  	res = new Double( Math.sin(f1.doubleValue()) );
	        else if  ( "tan".equals(op) )  	res = new Double( Math.tan(f1.doubleValue()) );
	        else if  ( "acos".equals(op) )  res = new Double( Math.acos(f1.doubleValue()) );
	        else if  ( "asin".equals(op) )  res = new Double( Math.asin(f1.doubleValue()) );
	        else if  ( "atan".equals(op) )  res = new Double( Math.atan(f1.doubleValue()) );
	        else if  ( "sqr".equals(op) )  	res = new Double( f1.doubleValue() * f1.doubleValue() );
	        else if  ( "sqrt".equals(op) )  res = new Double( Math.sqrt(f1.doubleValue()) );
	        else if  ( "log".equals(op) )  	res = new Double( Math.log(f1.doubleValue()) );
	        else if  ( "min".equals(op) )  	res = new Double( Math.min(f1.doubleValue(), f2.doubleValue()) );
	        else if  ( "max".equals(op) )  	res = new Double( Math.max(f1.doubleValue(), f2.doubleValue()) );
	        else if  ( "exp".equals(op) )  	res = new Double( Math.exp(f1.doubleValue()) );
	        else if  ( "floor".equals(op) ) res = new Double( Math.floor(f1.doubleValue()) );
	        else if  ( "ceil".equals(op) )  res = new Double( Math.ceil(f1.doubleValue()) );
	        else if  ( "abs".equals(op) )  	res = new Double( Math.abs(f1.doubleValue()) );
	        else if  ( "neg".equals(op) )  	res = new Double( - f1.doubleValue() );
	        else if  ( "rnd".equals(op) ) 	res = new Double( Math.random() * f1.doubleValue() );

	        return res;
	    }

	    private void initializeOperators()
	    {
	        operators     = new Operator[25];
	        operators[0]  = new Operator("+"	, 2, 0);
	        operators[1]  = new Operator("-"	, 2, 0);
	        operators[2]  = new Operator("*"	, 2, 10);
	        operators[3]  = new Operator("/"	, 2, 10);
	        operators[4]  = new Operator("^"	, 2, 10);
	        operators[5]  = new Operator("%"	, 2, 10);
	        operators[6]  = new Operator("&"	, 2, 0);
	        operators[7]  = new Operator("|"	, 2, 0);
	        operators[8]  = new Operator("cos" 	, 1, 20);
	        operators[9]  = new Operator("sin" 	, 1, 20);
	        operators[10] = new Operator("tan" 	, 1, 20);
	        operators[11] = new Operator("acos"	, 1, 20);
	        operators[12] = new Operator("asin"	, 1, 20);
	        operators[13] = new Operator("atan"	, 1, 20);
	        operators[14] = new Operator("sqrt"	, 1, 20);
	        operators[15] = new Operator("sqr" 	, 1, 20);
	        operators[16] = new Operator("log" 	, 1, 20);
	        operators[17] = new Operator("min" 	, 2, 0);
	        operators[18] = new Operator("max" 	, 2, 0);
	        operators[19] = new Operator("exp" 	, 1, 20);
	        operators[20] = new Operator("floor", 1, 20);
	        operators[21] = new Operator("ceil" , 1, 20);
	        operators[22] = new Operator("abs"  , 1, 20);
	        operators[23] = new Operator("neg" 	, 1, 20);
	        operators[24] = new Operator("rnd"  , 1, 20);
	    }

	    private Double getDouble(String s)
	    {
	    	if ( s == null ) return null;

	        Double res = null;
	        try {
	            res = Double.valueOf( s );
	        }
	        catch(Exception e) {
	        	return (Double) variables.get(s);
	        }

	        return res;
	    }

	    protected class Operator
	    {
	    	private String op;
	        private int type;
	        private int priority;

	        public Operator(String o, int t, int p)
	        {
	        	op = o;
	            type = t;
	            priority = p;
	    	}

	        public String getOperator() {
	        	return op;
	        }

	        public void setOperator(String o) {
	        	op = o;
	        }

	        public int getType() {
				return type;
	        }

	        public int getPriority() {
				return priority;
	        }
	    }

	    protected class Node
	    {
	        public String 	nString		= null;
	    	public Operator nOperator 	= null;
	        public Node 	nLeft		= null;
	        public Node 	nRight		= null;
	        public Node 	nParent		= null;
	        public int		nLevel		= 0;
	        public Double  	nValue		= null;

	    	public Node(String s) throws Exception
	        {
	        	init(null, s, 0);
	        }

	    	public Node(Node parent, String s, int level) throws Exception
	        {
	        	init(parent, s, level);
	        }

	        private void init(Node parent, String s, int level) throws Exception
	        {
	            s = removeIllegalCharacters(s);
	            s = removeBrackets(s);
	            s = addZero(s);
	        	if ( checkBrackets(s) != 0 ) throw new Exception("Wrong number of brackets in [" + s + "]");

	            nParent				= parent;
				nString 			= s;
	            nValue				= getDouble(s);
	            nLevel 				= level;
	            int sLength  		= s.length();
	            int inBrackets		= 0;
	            int startOperator   = 0;

	            for (int i=0; i<sLength; i++)
	            {
	        		if ( s.charAt(i) == '(' )
	                	inBrackets++;
	                else if ( s.charAt(i) == ')' )
	                	inBrackets--;
					else
	                {
	                    // the expression must be at "root" level
	                    if ( inBrackets == 0 )
	                    {
	                        Operator o = getOperator(nString,i);
	                        if ( o != null )
	                        {
	                        	// if first operator or lower priority operator
								if ( nOperator == null || nOperator.getPriority() >= o.getPriority() )
	                            {
	                            	nOperator 		= o;
	                            	startOperator 	= i;
	                            }
	                        }
	                    }
	                }
	            }

	            if ( nOperator != null )
	            {
	                // one operand, should always be at the beginning
	                if ( startOperator==0 && nOperator.getType() == 1 )
	                {
	                    // the brackets must be ok
	                    if ( checkBrackets( s.substring( nOperator.getOperator().length() ) ) == 0 )
	                    {
	                        nLeft  = new Node( this, s.substring( nOperator.getOperator().length() ) , nLevel + 1);
	                        nRight = null;
	                        return;
	                    }
	                    else
	                    	throw new Exception("Error during parsing... missing brackets in [" + s + "]");
	                }
	                // two operands
	                else if ( startOperator > 0 && nOperator.getType() == 2 )
	                {
	                    nOperator = nOperator;
	                    nLeft 	= new Node( this, s.substring(0,  startOperator), nLevel + 1 );
	                    nRight 	= new Node( this, s.substring(startOperator + nOperator.getOperator().length()), nLevel + 1);
	                }
	            }
	        }

	        private Operator getOperator(String s, int start)
	        {
	        	String temp = s.substring(start);
	            temp = getNextWord(temp);
	            for (int i=0; i<operators.length; i++)
	            {
	                if ( temp.startsWith(operators[i].getOperator()) )
	                    return operators[i];
	            }
	            return null;
	        }

	        private String getNextWord(String s)
	        {
	        	int sLength = s.length();
	            for (int i=1; i<sLength; i++)
	            {
	            	char c = s.charAt(i);
	                if ( (c > 'z' || c < 'a') && (c > '9' || c < '0') )
	                	return s.substring(0, i);
	            }
	            return s;
	    	}

	        /***
	         * checks if there is any missing brackets
	         * @return true if s is valid
	         */
	        protected int checkBrackets(String s)
	        {
	            int sLength  	= s.length();
	            int inBracket   = 0;

	            for (int i=0; i<sLength; i++)
	            {
	                if      ( s.charAt(i) == '(' && inBracket >= 0 )
	                    inBracket++;
	                else if ( s.charAt(i) == ')' )
	                    inBracket--;
	            }

	            return inBracket;
	        }

	        /***
	         * returns a string that doesnt start with a + or a -
	         */
	        protected String addZero(String s)
	        {
	        	if ( s.startsWith("+") || s.startsWith("-") )
	            {
	            	int sLength  	= s.length();
	                for (int i=0; i<sLength; i++)
	                {
	                	if ( getOperator(s, i) != null )
	                    	return "0" + s;
	                }
	            }
	            return s;
	        }

	        protected boolean hasChild() {
	        	return ( nLeft != null || nRight != null );
	        }

	        protected boolean hasOperator() {
	        	return ( nOperator != null );
	        }

	        protected boolean hasLeft() {
	        	return ( nLeft != null );
	        }
	        protected boolean hasRight() {
	        	return ( nRight != null );
	        }

	        /***
	         * Removes spaces, tabs and brackets at the begining
	         */
	        public String removeBrackets(String s)
	        {
	            String res = s;
	            if ( s.length() > 2 && res.startsWith("(") && res.endsWith(")") && checkBrackets(s.substring(1,s.length()-1)) == 0 )
	            {
	                res = res.substring(1, res.length()-1 );
	            }
	            if ( res != s )
	                return removeBrackets(res);
				else
	         	   return res;
	        }

	        /***
	         * Removes illegal characters
	         */
	        public String removeIllegalCharacters(String s)
	        {
	        	char[] illegalCharacters = { ' ' };
				String res = s;

	            for ( int j=0; j<illegalCharacters.length; j++)
	            {
	                int i = res.lastIndexOf(illegalCharacters[j], res.length());
	                while ( i != -1 )
	                {
	                    String temp = res;
	                    res = temp.substring(0,i);
	                    res += temp.substring(i + 1);
	                    i = res.lastIndexOf(illegalCharacters[j], s.length());
	                }
	            }
	            return res;
	        }
	    }
	}	
}
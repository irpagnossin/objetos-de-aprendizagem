package cepa.util;

public class ParametricCurve2D{
    
    private String[]  rToken = new String[2],
                     drToken = new String[2];
        
    private boolean[] useToken = new boolean[]{false,false};
    
    private MathExpression[] mathExpr = new MathExpression[2];
    private String[] stringExpr = new String[2];


    public ParametricCurve2D( String[] expression, String[] rToken, String[] drToken ){
        setExpression(expression);
        this.rToken = rToken;
        this.drToken = drToken;
        
        useToken[0] = true;
        useToken[1] = true;
    }    
    public ParametricCurve2D( String[] expression, String[] rToken ){
        setExpression(expression);
        this.rToken = rToken;
        
        useToken[0] = true;
        useToken[1] = false;        
    }    
    public ParametricCurve2D( String[] expression ){
        setExpression(expression);
        
        useToken[0] = false;
        useToken[1] = false;        
    }
    
    public void setExpression( String[] expression ){
        stringExpr = expression;
    }

    public void setContour( float[] r ){
        setContour(r,null);
    }
    public void setContour( float[] r, float[] dr ){
                
        String[] expression = new String[2];
        expression[0] = new String(stringExpr[0]);
        expression[1] = new String(stringExpr[1]);
        
        if ( useToken[1] && dr != null ){
            for ( int i = 1; i >= 0; i-- ){                  
                expression[0] = expression[0].replace( drToken[i], "(" + (new Float(dr[i])).toString() + ")" );
                expression[1] = expression[1].replace( drToken[i], "(" + (new Float(dr[i])).toString() + ")" );
            }        
        }
        
        if ( useToken[0] && r != null ){
            for ( int i = 1; i >= 0; i-- ){            
                expression[0] = expression[0].replace( rToken[i], "(" + (new Float(r[i])).toString() + ")" );
                expression[1] = expression[1].replace( rToken[i], "(" + (new Float(r[i])).toString() + ")" );
            }
        }
                
        //System.out.println( "expr = ( " + expression[0] + " , " + expression[1] + " )" );        
        
        mathExpr[0] = new MathExpression( expression[0] );
        mathExpr[1] = new MathExpression( expression[1] );
    }
    
    public float[] at ( float t ){
        return new float[]{
            (float) mathExpr[0].value(t),
            (float) mathExpr[1].value(t)
        };
    }

}


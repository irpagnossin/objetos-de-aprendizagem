package util;

import java.util.Vector;

/**
 *
 * @author Ivan Ramos Pagnossin
 */
public class ExtendedCubicBezier{
   
    public ExtendedCubicBezier( int dimension ){
        this.dimension = dimension; 
    }

    public boolean isEmpty() {
        return points.size() == 0;
    }
   
    public int size(){
        return points.size();
    }
    
    public float[] getPoint( int index ){
        return points.elementAt(index).clone();
    }
    
    public void set( float[] pF, int index ){
        set(pF,index,false);
    }
    
    public void specialSet( float[] pF, int index ){
        set(pF,index,true);
    }
    
    /**
     * Troca o ponto de índice <source>index</source> pelo ponto <source>point</source>.
     */
    private void set( float[] pF, int index, boolean special ){
        
        float[] pI = points.elementAt(index);
        
        points.set(index,pF);
        if (!special) initialState.set(index, false);
                
        if (isHandle(index)){
            
            int[] cPIndex = new int[]{ index-2, index-1, index+1, index+2 };
            
            for ( int i = 0; i < 4; i++ ){
                if ( cPIndex[i] >= 0 && cPIndex[i] <= points.size()-1 ){
                    if ( initialState.elementAt(cPIndex[i]) == true ){
                        reset(cPIndex[i]);
                    }
                    else if ( i == 1 || i == 2 ){
                        float[] p = new float[]{
                            points.elementAt(cPIndex[i])[0] + ( pF[0] - pI[0] ),
                            points.elementAt(cPIndex[i])[1] + ( pF[1] - pI[1] )
                        };

                        points.set(cPIndex[i], p);                    
                        if (!special) initialState.set(cPIndex[i], false);
                    }
                }
            }            
        }
        
        int sector = index / 3;
        if ( sector >= 0 && sector <= baseSet.size()-1 ) define(sector);
        
        if ( isHandle(index) ){
            ++sector;
            if ( sector >= 0 && sector <= baseSet.size()-1 ) define(sector);
        }
        
    }
    
    
    /**
     * Informa se o ponto de índice <source>index</source> é do tipo âncora (<source>true</source>) ou não
     * (<source>false</source>).
     */
    /*
     * Estratégia: todo ponto com índice 0 ou múltiplo de 3 é do tipo âncora (handle).
     */
    public boolean isHandle( int index ){
        return( index == 0 ? true : index % 3 == 0 );
    }

    /**
     * Adiciona um ponto do tipo âncora (handle), de coordenadas <source>hP1</source>.
     */
    /*
     * Estratégia: adiciona três pontos hP1 ao fim da lista, sendo os dois primeiros do tipo control-point
     * e o último, o handle propriamente dito. Em seguida, modifica os dois control-points para suas posições
     * padrão através do método reset.
     */    
    public void addHandle( float[] hP1 ){
               
        if (!points.isEmpty()){
            for ( int i = 1; i <= 3; i++ ){
                points.addElement(hP1);
                initialState.addElement(true);
            }

            reset(points.size()-3);
            reset(points.size()-2);
   
            define( getSector(points.size()-1) - 1 );
        }
        else{
            points.addElement(hP1);
            initialState.addElement(true);
        }
    }
    
    public int getSector( int index ){
        return index / 3;
    }
    
    /**
     * Retorna o índice da âncora associada ao ponto-de-controle de índice <source>index</source>.
     */
    public int getHandleIndex( int index ){
        if ( index <= 0 ) return 0;
        else if ( index % 3 == 0 ) return index;
        else if ( (index+1) % 3 == 0 ) return ++index;
        else return --index;
    }    
    
    public float[] getHandle( int index ){
        return points.elementAt(getHandleIndex(index)).clone();
    }
        
    /**
     * "Remove" o ponto de índice <source>index</source>. Se este ponto for uma âncora, delete-o
     * de fato juntamente com seus pontos-de-controle; se for um ponto-de-controle (não pode ser
     * apagado), retorna-o a sua posição inicial (sobre a reta que liga as duas âncoras adjacentes).
     */
    /*
     * Estratégia (ponto âncora): qualquer que seja a âncora, a idéia é remover três vezes o elemento de
     * índice index, valendo-se do remanejamento automático feito pelo objeto points. Se o ponto escolhido
     * for o primeiro (índice 0), retiramos três vezes esse elemento: na primeira execução, retira-se a
     * âncora propriamente dita; na segunda, apaga-se seu ponto-de-controle à direita (esta âncora não
     * tem um ponto-de-controle à esquerda); na terceira, o ponto-de-controle à esquerda da próxima âncora.
     * O raciocínio é similar para a última âncora. Já no caso de âncoras internas (que não sejam da 
     * extremidade), o raciocínio é o mesmo, exceto que precisamos remover o elemento de índice index-1.
     * Ou seja, primeiro removemos o ponto-de-controle à esquerda, depois a âncora e, em seguida, o ponto-
     * de-controle à direita. O condicional sobre isEmpty() protege-nos caso haja apenas uma âncora (neste
     * caso não há pontos-de-controle a excluir).
     * 
     * Estratégia (ponto-de-controle): por definição um ponto-de-controle não pode ser removido. Ao invés
     * disso, ele retorna à sua posição inicial.
     * 
     * Em qualquer um dos casos acima, é preciso redefinir os pontos-base no setor ao qual pertence a âncora
     * associada ao ponto removido e o setor imediatamente anterior.
     * 
     */
    public void delete(){
        points.removeAllElements();
        initialState.removeAllElements();
        baseSet.removeAllElements();
    }
    public void delete( int index ){

        int sector = index / 3;
        
        if (isHandle(index)){

            if ( index == 0 ){
                
                for ( int i = 1; i <= 3; i++ ){
                    if (!points.isEmpty()){
                        points.removeElementAt(0);
                        initialState.removeElementAt(0);
                    }
                }      
                
                if ( !baseSet.isEmpty() ) baseSet.removeElementAt(sector);
            }
            else if ( index == points.size()-1 ){
                
                for ( int i = 1; i <= 3; i++ ){
                    if (!points.isEmpty()){
                        points.removeElementAt(points.size()-1);
                        initialState.removeElementAt(initialState.size()-1);
                    }
                }
                
                baseSet.removeElementAt(--sector);
            }
            else{
                
                for ( int i = 1; i <= 3; i++ ){
                    if (!points.isEmpty()){
                        points.removeElementAt(index-1);
                        initialState.removeElementAt(index-1);
                    }
                }
            
                baseSet.removeElementAt(sector);
                
                int[] cPIndex = new int[]{ index - 1, index - 2 };
                for ( int i = 0; i < 2; i++ ) if ( initialState.elementAt(cPIndex[i]) ) reset(cPIndex[i]);                
                
                define(--sector);
            }
        }
        else{
            reset(index);
            initialState.set(index, true);
            define(sector);
        }
    }
    
    /**
     * Retorna o valor máximo do parâmetro <source>t</source> na definição da curva Bezier. O valor mínimo
     * é sempre zero.
     */
    public int getRange(){
        return points.size() / 3;
    }
        
    /**
     * Retorna o ponto da curva Bezier definido pelo parâmetro <source>t</source>.
     */    
    public float[] at( float t ){        
                
        t = Math.max( 0, Math.min(t,getRange()) );  // Mantém t no intervalo [0,getRange()]
                
        int sector = (int)t;                        // Identifica o setor a ser utilizado.
        if ( sector == baseSet.size() ) --sector;   // Quando t == getRange(), o setor a ser utilizado é sector-1,
        t -= sector;                                // Normaliza t para o intervalo [0,1].
        
        float a = 0, b = 0, c = 0, d = 0;

        float[] vector = new float[dimension];
        for ( int i = 0; i < dimension; i++ ){ 
            a = baseSet.elementAt(sector).get(0)[i];
            b = baseSet.elementAt(sector).get(1)[i];
            c = baseSet.elementAt(sector).get(2)[i];
            d = baseSet.elementAt(sector).get(3)[i];
            vector[i] = (float)(a * Math.pow(t,3) + b * Math.pow(t,2) + c * t + d);
        }

        return vector;
    }
 
    /**
     * Retorna a derivada da curva Bezier no ponto definido pelo parâmetro <source>t</source>.
     */
    public float[] derivative( float t ){
        
        t = Math.max( 0, Math.min(t,getRange()) );  // Mantém t no intervalo [0,getRange()]
        
        int sector = (int)t;                        // Identifica o setor a ser utilizado.
        if ( sector == baseSet.size() ) --sector;   // Quando t == getRange(), o setor a ser utilizado é sector-1,
        t -= sector;                                // Normaliza t para o intervalo [0,1].
        
        float a = 0, b = 0, c = 0;
        
        float[] vector = new float[dimension];
        for ( int i = 0; i < dimension; i++ ){
            a = baseSet.elementAt(sector).get(0)[i];
            b = baseSet.elementAt(sector).get(1)[i];
            c = baseSet.elementAt(sector).get(2)[i];           
            vector[i] = (float)(3 * a * Math.pow(t,2) + 2 * b * t + c);
        }        
       return vector;
    }    
    
    // Membros privados.
    

    private int dimension = 1;

    private Vector<float[]> points = new Vector<float[]>();
    private Vector<BaseSet> baseSet  = new Vector<BaseSet>();
    private Vector<Boolean> initialState = new Vector<Boolean>();
    
    private static final float[][] BASIS = {
        {-1  ,  3  , -3  , 1  },
        { 3  , -6  ,  3  , 0  },
        {-3  ,  3  ,  0  , 0  },
        { 1  ,  0  ,  0  , 0  } 
    };     
    

    private void define( int sector ){

        /*if ( sector < 0 || sector >= getRange() ){
            throw new IllegalArgumentException( "O setor (= " + sector + ") deve pertencer ao intervalo [0," + getRange() + ")." );
        }*/
                                
        float[] a = new float[dimension],
                b = new float[dimension],
                c = new float[dimension],
                d = new float[dimension];
                
        for ( int i = 0; i < dimension; i++ ){
            a[i] = b[i] = c[i] = d[i] = 0f;
        }
        
        int offset = 3 * sector;
        for ( int i = 0; i < dimension; i++ ){
            for ( int j = 0; j < 4; j++ ){
                float point = points.elementAt(j+offset)[i];
                a[i] += BASIS[0][j] * point;
                b[i] += BASIS[1][j] * point;
                c[i] += BASIS[2][j] * point;
                d[i] += BASIS[3][j] * point;
            }
        }        
        
        // Caso esteja sendo criado um novo setor, adiciona-o no final do Vector baseSet.
        if ( sector > baseSet.size()-1 ){
            baseSet.add(new BaseSet(2));
        }
                
        baseSet.elementAt(sector).set(0,a);
        baseSet.elementAt(sector).set(1,b);
        baseSet.elementAt(sector).set(2,c);
        baseSet.elementAt(sector).set(3,d);
    }
    
    /**
     * Retorna o ponto-de-controle de índice <source>cPIndex</source> para sua posição inicial, a saber: sobre a reta que liga
     * as âncoras à direita e à esquerda. Este método não tem efeito sobre os pontos do tipo âncora.
     */
    /*
     * Estratégia: primeiramente, procura pelos (dois) handles à direita e à esquerda do control-point referido
     * (índice cPIndex). Em seguida, redefine sua posição de modo a colocá-lo sobre o segmento de reta que liga
     * os dois handles. A distância entre o control-point e seu handle é de 1/4 da distância entre os dois handles.
     */
    private void reset( int cPIndex ){
        if (!isHandle(cPIndex)){
            int hPIndex = getHandleIndex(cPIndex), factor = +1;

            if ( hPIndex > cPIndex ) factor = -1;
            
            float[] hP1 = points.elementAt(hPIndex);
            float[] hP2 = points.elementAt(hPIndex + 3*factor);
           
            float angle = (float)(Math.atan2(hP2[1]-hP1[1],hP2[0]-hP1[0]));
            float distance = (float)(Math.sqrt( Math.pow(hP1[0]-hP2[0],2) + Math.pow(hP1[1]-hP2[1],2) ));
                                    
            points.set(cPIndex, new float[]{
                (float)(hP1[0] + 0.25 * distance * Math.cos(angle)),
                (float)(hP1[1] + 0.25 * distance * Math.sin(angle)),
            });
            
        }
    }    
    
    private class BaseSet{
        
        private Vector<float[]> points = new Vector<float[]>(4);
        private int dimension = 1;
        
        public BaseSet( int dimension ){
            this.dimension = dimension;
            reset();
        }
        
        public void set( int index, float[] point ){
            if ( index >= 0 && index <=4 ){
                points.set(index, point);
            }
        }
        
        public float[] get( int index ){
            return points.get(index);
        }
       
        public void reset(){
            float[] pInitial = new float[dimension];
            for ( int i = 0; i < dimension; i++ ) pInitial[i] = 0f;
            for ( int i = 1; i <= 4; i++ ) points.add(pInitial);            
        }
    }
    
}

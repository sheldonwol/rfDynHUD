package net.ctdp.rfdynhud.input;

import net.ctdp.rfdynhud.widgets.widget.Widget;

public class InputMappings
{
    private final InputMapping[] mappings;
    
    /**
     * Gets the {@link InputAction}s possibly mapped to an action on the given {@link Widget}.
     * 
     * @param widget
     * 
     * @return the possibly mapped actions
     */
    public final InputAction[] getMappedActions( Widget widget )
    {
        final String widgetName = widget.getName();
        
        int n = 0;
        
        for ( int i = 0 ; i < mappings.length; i++ )
        {
            if ( widgetName.equals( mappings[i] ) )
                n++;
        }
        
        if ( n == 0 )
            return ( null );
        
        InputAction[] result = new InputAction[ n ];
        
        int j = 0;
        for ( int i = 0 ; i < mappings.length; i++ )
        {
            if ( widgetName.equals( mappings[i] ) )
                result[j++] = mappings[i].getAction();
        }
        
        return ( result );
    }
    
    /**
     * Gets whether an input mapping exists, that maps an input device component to the given {@link InputAction}.
     *  
     * @param action
     * 
     * @return whether an input mapping exists, that maps an input device component to the given {@link InputAction}.
     */
    public final boolean isActionMapped( InputAction action )
    {
        for ( int i = 0 ; i < mappings.length; i++ )
        {
            if ( mappings[i].getAction().equals( action ) )
                return ( true );
        }
        
        return ( false );
    }
    
    /**
     * Gets the input device components mapped to the given {@link InputAction} on a {@link Widget} with the name of the given {@link Widget}.
     * 
     * @param action
     * @param widget
     * 
     * @return the input device components mapped to the given {@link InputAction} on a {@link Widget} with the name of the given {@link Widget}.
     */
    public final String[] getMappedDeviceComponents( InputAction action, Widget widget )
    {
        final String widgetName = widget.getName();
        final boolean isWidgetAction = action.isWidgetAction();
        
        int n = 0;
        
        for ( int i = 0 ; i < mappings.length; i++ )
        {
            InputMapping mapping = mappings[i];
            InputAction action2 = mapping.getAction();
            
            if ( mapping.getAction().equals( action ) )
            {
                if ( isWidgetAction )
                {
                    if ( action2.isWidgetAction() && mapping.getWidgetName().equals( widgetName ) )
                    {
                        n++;
                    }
                }
                else if ( !action2.isWidgetAction() )
                {
                    n++;
                }
            }
        }
        
        if ( n == 0 )
            return ( null );
        
        String[] result = new String[ n ];
        
        int j = 0;
        for ( int i = 0 ; i < mappings.length; i++ )
        {
            InputMapping mapping = mappings[i];
            InputAction action2 = mapping.getAction();
            
            if ( mapping.getAction().equals( action ) )
            {
                if ( isWidgetAction )
                {
                    if ( action2.isWidgetAction() && mapping.getWidgetName().equals( widgetName ) )
                    {
                        result[j++] = mappings[i].getDeviceComponent();
                    }
                }
                else if ( !action2.isWidgetAction() )
                {
                    result[j++] = mappings[i].getDeviceComponent();
                }
            }
        }
        
        return ( result );
    }
    
    InputMappings( InputMapping[] mappings )
    {
        this.mappings = mappings;
    }
}
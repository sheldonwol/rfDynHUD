/**
 * Copyright (C) 2009-2010 Cars and Tracks Development Project (CTDP).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.ctdp.rfdynhud.lessons.widgets.lesson6;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.Wheel;
import net.ctdp.rfdynhud.gamedata.ProfileInfo.MeasurementUnits;
import net.ctdp.rfdynhud.lessons.widgets._util.LessonsWidgetSet;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.NumberUtil;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.widgets.widget.Widget;
import net.ctdp.rfdynhud.widgets.widget.WidgetPackage;

/**
 * This Widget demonstrates the use of localizations.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class Lesson6Widget_Localizations extends Widget
{
    private DrawnString ds = null;
    
    private final FloatValue v = new FloatValue( -1f, 0.1f );
    
    @Override
    public int getVersion()
    {
        return ( composeVersion( 1, 0, 0 ) );
    }
    
    @Override
    public WidgetPackage getWidgetPackage()
    {
        return ( LessonsWidgetSet.WIDGET_PACKAGE );
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        v.reset();
    }
    
    private static final String getTempUnits( MeasurementUnits measurementUnits )
    {
        /*
         * We need to decide, which units are being used
         * and return the appropriate localization string.
         */
        
        if ( measurementUnits == MeasurementUnits.IMPERIAL )
            return ( Loc.temperature_units_IMPERIAL );
        
        return ( Loc.temperature_units_METRIC );
    }
    
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final MeasurementUnits measurementUnits = gameData.getProfileInfo().getMeasurementUnits();
        
        /*
         * We set the caption as the prefix and units string as a postfix.
         */
        ds = drawnStringFactory.newDrawnString( "ds", 0, 0, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor(), Loc.mytext_caption + ": ", getTempUnits( measurementUnits ) );
    }
    
    @Override
    protected void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        v.update( gameData.getTelemetryData().getTireTemperature( Wheel.FRONT_LEFT ) );
        
        if ( needsCompleteRedraw || ( clock1 && v.hasChanged() ) )
        {
            String tireTempFL = NumberUtil.formatFloat( v.getValue(), 1, true );
            
            ds.draw( offsetX, offsetY, tireTempFL, texture );
        }
    }
    
    public Lesson6Widget_Localizations( String name )
    {
        super( name, 14.0f, 5.0f );
    }
}
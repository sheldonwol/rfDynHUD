package net.ctdp.rfdynhud.etv2010.widgets.standings;

import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.Laptime;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.StandingsTools;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.values.StandingsView;
import net.ctdp.rfdynhud.values.StringValue;
import net.ctdp.rfdynhud.widgets.widget.Widget;

/**
 * The {@link ETVStandingsWidget} displays the list of drivers and gaps.
 * 
 * @author Marvin Froehlich
 */
public class ETVStandingsWidget extends Widget
{
    private final ColorProperty captionBackgroundColor = new ColorProperty( this, "captionBgColor", ETVUtils.ETV_STYLE_CAPTION_BACKGROUND_COLOR );
    private final ColorProperty captionBackgroundColor1st = new ColorProperty( this, "captionBgColor1st", ETVUtils.ETV_STYLE_CAPTION_BACKGROUND_COLOR_1ST );
    private final ColorProperty captionColor = new ColorProperty( this, "captionColor", ETVUtils.ETV_STYLE_CAPTION_FONT_COLOR );
    private final ColorProperty dataBackgroundColor1st = new ColorProperty( this, "dataBgColor1st", ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR_1ST );
    
    private final BooleanProperty forceLeaderDisplayed = new BooleanProperty( this, "forceLeaderDisplayed", true );
    private final BooleanProperty showFastestLapsInRace = new BooleanProperty( this, "showFastestLapsInRace", true );
    
    private DrawnString[] captionStrings = null;
    private DrawnString[] nameStrings = null;
    private DrawnString[] gapStrings = null;
    
    private IntValue[] positions = null;
    private StringValue[] driverNames = null;
    private FloatValue[] gaps = null;
    
    private int maxNumItems = 0;
    
    private int oldNumItems = 0;
    
    private Boolean[] itemsVisible = null;
    
    private final Size itemHeight = new Size( 0, Size.PERCENT_OFFSET + 0.025f, this, true );
    
    private TextureImage2D itemClearImage = null;
    
    private static final int NUM_FLAG_TEXTURES = 3;
    
    private TransformableTexture[] flagTextures = null;
    private final FloatValue[] laptimes = new FloatValue[ NUM_FLAG_TEXTURES ];
    private final DrawnString[] laptimeStrings = new DrawnString[ NUM_FLAG_TEXTURES ];
    
    private VehicleScoringInfo[] vehicleScoringInfos = null;
    
    private int oldNumVehicles = -1;
    
    private boolean isOnLeftSide = true;
    
    private IntValue[] lap = null;
    private float displayTime;
    private int lastVisibleIndex = -1;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedColorValue( String name )
    {
        String result = super.getDefaultNamedColorValue( name );
        
        if ( result != null )
            return ( result );
        
        return ( ETVUtils.getDefaultNamedColorValue( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultNamedFontValue( String name )
    {
        String result = super.getDefaultNamedFontValue( name );
        
        if ( result != null )
            return ( result );
        
        return ( ETVUtils.getDefaultNamedFontValue( name ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void bake()
    {
        super.bake();
        
        itemHeight.bake();
    }
    
    public void setAllPosAndSizeToPercents()
    {
        super.setAllPosAndSizeToPercents();
        
        if ( !itemHeight.isHeightPercentageValue() )
            itemHeight.flipHeightPercentagePx();
    }
    
    public void setAllPosAndSizeToPixels()
    {
        super.setAllPosAndSizeToPixels();
        
        if ( itemHeight.isHeightPercentageValue() )
            itemHeight.flipHeightPercentagePx();
    }
    
    @Override
    public String getWidgetPackage()
    {
        return ( ETVUtils.WIDGET_PACKAGE );
    }
    
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        if ( driverNames != null )
        {
            for ( int i = 0; i < driverNames.length; i++ )
            {
                positions[i].reset();
                driverNames[i].reset();
                gaps[i].reset();
                lap[i].reset();
            }
        }
        
        lastVisibleIndex = -1;
        
        forceReinitialization();
    }
    
    @Override
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onRealtimeEntered( gameData, editorPresets );
        
        if ( laptimes != null )
        {
            for ( int i = 0; i < laptimes.length; i++ )
            {
                if ( laptimes[i] != null )
                    laptimes[i].reset();
            }
        }
        
        oldNumVehicles = -1;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        if ( gameData.getScoringInfo().getSessionType().isRace() && !showFastestLapsInRace.getBooleanValue() )
        {
            flagTextures = null;
            
            return ( null );
        }
        
        int itemHeight = this.itemHeight.getEffectiveHeight();
        
        if ( ( flagTextures == null ) || ( flagTextures[0].getWidth() != widgetInnerWidth ) || ( flagTextures[0].getHeight() != itemHeight ) )
        {
            flagTextures = new TransformableTexture[ NUM_FLAG_TEXTURES ];
            
            for ( int i = 0; i < NUM_FLAG_TEXTURES; i++ )
            {
                flagTextures[i] = new TransformableTexture( widgetInnerWidth, itemHeight );
            }
        }
        
        return ( flagTextures );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onBoundInputStateChanged( boolean isEditorMode, InputAction action, boolean state, int modifierMask )
    {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        int numVehicles = gameData.getScoringInfo().getNumVehicles();
        
        boolean result = ( numVehicles != oldNumVehicles );
        
        oldNumVehicles = numVehicles;
        
        return ( result );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        int itemHeight = this.itemHeight.getEffectiveHeight();
        maxNumItems = ( height + ETVUtils.ITEM_GAP ) / ( itemHeight + ETVUtils.ITEM_GAP );
        
        vehicleScoringInfos = new VehicleScoringInfo[ maxNumItems ];
        
        if ( ( itemClearImage == null ) || ( itemClearImage.getWidth() != width ) || ( itemClearImage.getHeight() != itemHeight * 2 ) )
        {
            itemClearImage = TextureImage2D.createOfflineTexture( width, itemHeight * 2, true );
            
            ETVUtils.drawLabeledDataBackground( 0, 0, width, itemHeight, "00", getFont(), captionBackgroundColor1st.getColor(), dataBackgroundColor1st.getColor(), itemClearImage, true );
            ETVUtils.drawLabeledDataBackground( 0, itemHeight, width, itemHeight, "00", getFont(), captionBackgroundColor.getColor(), getBackgroundColor(), itemClearImage, true );
        }
        
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( getFont() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D numBounds = metrics.getStringBounds( "00", texCanvas );
        
        int capWidth = (int)Math.ceil( numBounds.getWidth() );
        int dataAreaLeft = ETVUtils.getLabeledDataDataLeft( width, numBounds );
        int dataAreaRight = ETVUtils.getLabeledDataDataRight( width );
        int vMiddle = ETVUtils.getLabeledDataVMiddle( itemHeight, numBounds );
        
        captionStrings = new DrawnString[ maxNumItems ];
        nameStrings = new DrawnString[ maxNumItems ];
        gapStrings = new DrawnString[ maxNumItems ];
        
        positions = new IntValue[ maxNumItems ];
        driverNames = new StringValue[ maxNumItems ];
        gaps = new FloatValue[ maxNumItems ];
        
        lap = new IntValue[ maxNumItems ];
        
        itemsVisible = new Boolean[ maxNumItems ];
        
        for ( int i = 0; i < maxNumItems; i++ )
        {
            captionStrings[i] = new DrawnString( ETVUtils.TRIANGLE_WIDTH + capWidth, vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
            nameStrings[i] = new DrawnString( dataAreaLeft, vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), getFontColor() );
            gapStrings[i] = new DrawnString( dataAreaRight, vMiddle, Alignment.RIGHT, false, getFont(), isFontAntiAliased(), getFontColor() );
            
            positions[i] = new IntValue();
            driverNames[i] = new StringValue();
            gaps[i] = new FloatValue();
            
            lap[i] = new IntValue();
            
            itemsVisible[i] = null;
        }
        
        TransformableTexture[] flagTextures = getSubTexturesImpl( gameData, editorPresets,  width, height );
        
        if ( flagTextures != null )
        {
            for ( int i = 0; i < NUM_FLAG_TEXTURES; i++ )
            {
                ETVUtils.drawDataBackground( 0, 0, flagTextures[i].getWidth(), flagTextures[i].getHeight(), getBackgroundColor(), flagTextures[i].getTexture(), true );
                
                laptimes[i] = new FloatValue();
                laptimeStrings[i] = new DrawnString( flagTextures[i].getWidth() / 2, vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
            }
        }
        
        isOnLeftSide = ( getPosition().getEffectiveX() < getConfiguration().getGameResX() - getPosition().getEffectiveX() - getSize().getEffectiveWidth() );
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        texture.clear( offsetX, offsetY, width, height, true, null );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final boolean isEditorMode = ( editorPresets != null );
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        final int itemHeight = this.itemHeight.getEffectiveHeight();
        
        final int numDrivers = StandingsTools.getDisplayedVSIsForScoring( scoringInfo, StandingsView.RELATIVE_TO_LEADER, forceLeaderDisplayed.getBooleanValue(), vehicleScoringInfos );
        int numDisplayedLaptimes = 0;
        
        if ( flagTextures != null )
        {
            for ( int i = 0; i < flagTextures.length; i++ )
            {
                flagTextures[i].setVisible( false );
            }
        }
        
        if ( scoringInfo.getSessionTime() > displayTime )
        {
            lastVisibleIndex = -1;
        }
        
        int i2 = 0;
        if ( ( numDrivers > 1 ) && ( vehicleScoringInfos[1].getPlace() - vehicleScoringInfos[0].getPlace() > 1 ) )
        {
            i2 = 1;
        }
        
        for ( int i = 0; i < numDrivers; i++ )
        {
            VehicleScoringInfo vsi = vehicleScoringInfos[i];
            
            lap[i].update( vsi.getCurrentLap() );
            
            if ( lap[i].hasChanged() )
            {
                if ( ( i == 0 ) || ( i == i2 ) )
                {
                    lastVisibleIndex = 0;
                    displayTime = scoringInfo.getSessionTime() + 40f;
                }
                else if ( scoringInfo.getSessionTime() <= displayTime )
                {
                    lastVisibleIndex = Math.max( lastVisibleIndex, i );
                    displayTime = Math.max( displayTime, scoringInfo.getSessionTime() + 20f );
                }
            }
        }
        
        for ( int i = 0; i < numDrivers; i++ )
        {
            VehicleScoringInfo vsi = vehicleScoringInfos[i];
            
            Boolean visible;
            if ( isEditorMode )
            {
                visible = true;
            }
            else
            {
                if ( scoringInfo.getSessionType().isRace() )
                    visible = ( i <= lastVisibleIndex );
                else
                    visible = ( vsi.getBestLapTime() > 0.0f );
            }
            
            boolean drawBackground = needsCompleteRedraw;
            boolean visibilityChanged = false;
            
            if ( visible != itemsVisible[i] )
            {
                itemsVisible[i] = visible;
                drawBackground = true;
                visibilityChanged = true;
            }
            
            int offsetY2 = i * ( itemHeight + ETVUtils.ITEM_GAP );
            int srcOffsetY = ( vsi.getPlace() == 1 ) ? 0 : itemHeight;
            
            if ( drawBackground )
            {
                if ( visible )
                    texture.clear( itemClearImage, 0, srcOffsetY, width, itemHeight, offsetX, offsetY + offsetY2, width, itemHeight, true, null );
                else
                    texture.clear( offsetX, offsetY + offsetY2, width, itemHeight, true, null );
            }
            
            positions[i].update( vsi.getPlace() );
            
            if ( ( needsCompleteRedraw || visibilityChanged || positions[i].hasChanged() ) && visible )
            {
                try
                {
                    captionStrings[i].draw( offsetX, offsetY + offsetY2, positions[i].getValueAsString(), itemClearImage, offsetX, offsetY + offsetY2 - srcOffsetY, getFontColor(), texture );
                }
                catch ( Throwable t )
                {
                }
            }
            
            driverNames[i].update( vsi.getDriverNameTLC() );
            
            if ( ( needsCompleteRedraw || visibilityChanged || driverNames[i].hasChanged() ) && visible )
            {
                try
                {
                    nameStrings[i].draw( offsetX, offsetY + offsetY2, driverNames[i].getValue(), itemClearImage, offsetX, offsetY + offsetY2 - srcOffsetY, getFontColor(), texture );
                }
                catch ( Throwable t )
                {
                }
            }
            
            if ( vsi.getPlace() > 1 )
            {
                if ( scoringInfo.getSessionType() == SessionType.RACE )
                    gaps[i].update( ( vsi.getLapsBehindLeader() > 0 ) ? -vsi.getLapsBehindLeader() - 10000 : -vsi.getTimeBehindLeader() );
                else
                    gaps[i].update( vsi.getBestLapTime() - scoringInfo.getVehicleScoringInfo( 0 ).getBestLapTime() );
                
                if ( ( needsCompleteRedraw || visibilityChanged || gaps[i].hasChanged() ) && visible )
                {
                    String s;
                    if ( vsi.getBestLapTime() < 0.0f )
                        s = "";
                    else
                        s = ( gaps[i].getValue() < -10000f ) ? "+" + ( (int)-( gaps[i].getValue() + 10000.0f ) ) + "Lap(s)" : TimingUtil.getTimeAsGapString( gaps[i].getValue() );
                    
                    try
                    {
                        gapStrings[i].draw( offsetX, offsetY + offsetY2, s, itemClearImage, offsetX, offsetY + offsetY2 - srcOffsetY, getFontColor(), texture );
                    }
                    catch ( Throwable t )
                    {
                    }
                }
            }
            
            if ( flagTextures != null )
            {
                if ( !isEditorMode && visible && ( numDisplayedLaptimes < flagTextures.length - 1 ) )
                {
                    Laptime lt = vsi.getFastestLaptime();
                    if ( ( lt != null ) && ( lt.getLap() == vsi.getCurrentLap() - 1 ) && ( vsi.getStintStartLap() != vsi.getCurrentLap() ) && ( scoringInfo.getSessionTime() - vsi.getLapStartTime() < 20.0f ) )
                    {
                        int tti = numDisplayedLaptimes++;
                        TransformableTexture tt = flagTextures[tti];
                        
                        laptimes[tti].update( lt.getLapTime() );
                        
                        if ( laptimes[tti].hasChanged() )
                        {
                            laptimeStrings[tti].draw( 0, 0, TimingUtil.getTimeAsString( laptimes[tti].getValue(), true ), getBackgroundColor(), tt.getTexture() );
                        }
                        
                        if ( isOnLeftSide )
                            tt.setTranslation( width - ( ETVUtils.TRIANGLE_WIDTH / 2.0f ), offsetY2 );
                        else
                            tt.setTranslation( -width + ( ETVUtils.TRIANGLE_WIDTH / 2.0f ), offsetY2 );
                        tt.setVisible( true );
                    }
                }
            }
        }
        
        for ( int i = numDrivers; i < oldNumItems; i++ )
        {
            int offsetY2 = i * ( itemHeight + ETVUtils.ITEM_GAP );
            
            texture.clear( offsetX, offsetY + offsetY2, width, itemHeight, true, null );
        }
        
        oldNumItems = numDrivers;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( captionBackgroundColor, "The background color for the \"Position\" caption." );
        writer.writeProperty( captionBackgroundColor1st, "The background color for the \"Position\" caption for first place." );
        writer.writeProperty( captionColor, "The font color for the \"Lap\" caption." );
        writer.writeProperty( dataBackgroundColor1st, "The background color for the data area, for first place." );
        writer.writeProperty( "itemHeight", Size.unparseValue( itemHeight.getHeight() ), "The height of one item." );
        writer.writeProperty( forceLeaderDisplayed, "Display leader regardless of maximum displayed drivers setting?" );
        writer.writeProperty( showFastestLapsInRace, "Display fastest lap flags in race session?" );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( captionBackgroundColor.loadProperty( key, value ) );
        else if ( captionBackgroundColor1st.loadProperty( key, value ) );
        else if ( captionColor.loadProperty( key, value ) );
        else if ( dataBackgroundColor1st.loadProperty( key, value ) );
        else if ( itemHeight.loadProperty( key, value, "sdfsdfsdfsdf", "itemHeight" ) );
        else if ( forceLeaderDisplayed.loadProperty( key, value ) );
        else if ( showFastestLapsInRace.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont )
    {
        super.getProperties( propsCont );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( captionBackgroundColor );
        propsCont.addProperty( captionBackgroundColor1st );
        propsCont.addProperty( captionColor );
        propsCont.addProperty( dataBackgroundColor1st );
        propsCont.addProperty( itemHeight.createHeightProperty( "itemHeight" ) );
        propsCont.addProperty( forceLeaderDisplayed );
        propsCont.addProperty( showFastestLapsInRace );
    }
    
    /*
    @Override
    protected boolean hasText()
    {
        return ( false );
    }
    */
    
    @Override
    protected boolean canHaveBorder()
    {
        return ( false );
    }
    
    public ETVStandingsWidget( String name )
    {
        super( name, Size.PERCENT_OFFSET + 0.14f, Size.PERCENT_OFFSET + ( 0.025f * 10f ) );
        
        getBackgroundColorProperty().setValue( ETVUtils.ETV_STYLE_DATA_BACKGROUND_COLOR );
        getFontColorProperty().setValue( ETVUtils.ETV_STYLE_DATA_FONT_COLOR );
        getFontProperty().setValue( ETVUtils.ETV_STYLE_FONT );
    }
}
package net.ctdp.rfdynhud.etv2010.widgets.sessionstate;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.etv2010.widgets._base.ETVWidgetBase;
import net.ctdp.rfdynhud.etv2010.widgets._util.ETVUtils;
import net.ctdp.rfdynhud.gamedata.GamePhase;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionLimit;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.YellowFlagState;
import net.ctdp.rfdynhud.properties.EnumProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.DrawnString.Alignment;
import net.ctdp.rfdynhud.util.TimingUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.BoolValue;
import net.ctdp.rfdynhud.values.EnumValue;
import net.ctdp.rfdynhud.values.FloatValue;
import net.ctdp.rfdynhud.values.IntValue;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

/**
 * The {@link ETVSessionStateWidget} displays the current lap.
 * 
 * @author Marvin Froehlich
 */
public class ETVSessionStateWidget extends ETVWidgetBase
{
    private final EnumProperty<SessionLimit> sessionLimitPreference = new EnumProperty<SessionLimit>( this, "sessionLimitPreference", SessionLimit.LAPS );
    
    private SessionLimit sessionLimit = SessionLimit.LAPS;
    
    private DrawnString captionString = null;
    private DrawnString stateString = null;
    
    private String caption = getCaption( SessionType.RACE, SessionLimit.LAPS );
    
    private final EnumValue<GamePhase> gamePhase = new EnumValue<GamePhase>();
    private final EnumValue<YellowFlagState> yellowFlagState = new EnumValue<YellowFlagState>( YellowFlagState.NONE );
    private final BoolValue sectorYellowFlag = new BoolValue();
    
    private final IntValue lap = new IntValue();
    private final FloatValue sessionTime = new FloatValue( -1f, 0.1f );
    
    private Color dataBgColor = Color.MAGENTA;
    private Color dataFontColor = Color.GREEN;
    
    private static final Alignment[] colAligns = new Alignment[] { Alignment.RIGHT, Alignment.CENTER, Alignment.RIGHT };
    private final int[] colWidths = new int[ 3 ];
    private static final int colPadding = 10;
    
    private static final String getCaption( SessionType sessionType, SessionLimit sessionLimit )
    {
        switch ( sessionType )
        {
            case TEST_DAY:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_TEST_DAY_time );
                
                return ( Loc.caption_TEST_DAY_laps );
            case PRACTICE1:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_PRACTICE1_time );
                
                return ( Loc.caption_PRACTICE1_laps );
            case PRACTICE2:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_PRACTICE2_time );
                
                return ( Loc.caption_PRACTICE2_laps );
            case PRACTICE3:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_PRACTICE3_time );
                
                return ( Loc.caption_PRACTICE3_laps );
            case PRACTICE4:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_PRACTICE4_time );
                
                return ( Loc.caption_PRACTICE4_laps );
            case QUALIFYING:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_QUALIFYING_time );
                
                return ( Loc.caption_QUALIFYING_laps );
            case WARMUP:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_WARMUP_time );
                
                return ( Loc.caption_WARMUP_laps );
            case RACE:
                if ( sessionLimit == SessionLimit.TIME )
                    return ( Loc.caption_RACE_time );
                
                return ( Loc.caption_RACE_laps );
        }
        
        // Unreachable code!
        return ( "N/A" );
    }
    
    private boolean updateSessionLimit( LiveGameData gameData )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        SessionLimit oldSessionLimit = sessionLimit;
        String oldCaption = caption;
        
        sessionLimit = scoringInfo.getViewedVehicleScoringInfo().getSessionLimit( sessionLimitPreference.getEnumValue() );
        caption = getCaption( scoringInfo.getSessionType(), sessionLimit );
        
        if ( ( sessionLimit != oldSessionLimit ) || !caption.equals( oldCaption ) )
        {
            //Logger.log( ">> sessionLimit changed: " + sessionLimit + ", " + caption );
            
            forceReinitialization();
            forceCompleteRedraw();
            
            return ( true );
        }
        
        return ( false );
    }
    
    @Override
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.afterConfigurationLoaded( widgetsConfig, gameData, editorPresets );
        
        //Logger.log( "afterConfigurationLoaded(): " + gameData.getScoringInfo().getMaxLaps() + ", " + gameData.getScoringInfo().getEndTime() );
        //updateSessionLimit( gameData );
    }
    
    @Override
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
        super.onSessionStarted( sessionType, gameData, editorPresets );
        
        //Logger.log( "onSessionStarted(): " + gameData.getScoringInfo().getSessionType() + ", " + gameData.getScoringInfo().getMaxLaps() + ", " + gameData.getScoringInfo().getEndTime() );
        
        yellowFlagState.reset();
        sectorYellowFlag.reset();
        lap.reset();
        sessionTime.reset();
        gamePhase.reset();
        
        //updateSessionLimit( gameData );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        gamePhase.update( scoringInfo.getGamePhase() );
        yellowFlagState.update( scoringInfo.getYellowFlagState() );
        sectorYellowFlag.update( scoringInfo.getSectorYellowFlag( scoringInfo.getViewedVehicleScoringInfo().getSector() ) );
        
        boolean changed = false;
        if ( gamePhase.hasChanged() )
            changed = true;
        if ( yellowFlagState.hasChanged() )
            changed = true;
        if ( sectorYellowFlag.hasChanged() )
            changed = true;
        
        dataBgColor = getBackgroundColor();
        dataFontColor = getFontColor();
        if ( ( gamePhase.getValue() == GamePhase.FORMATION_LAP ) || ( gamePhase.getValue() == GamePhase.FULL_COURSE_YELLOW ) || sectorYellowFlag.getValue() )
        {
            dataBgColor = Color.YELLOW;
            dataFontColor = Color.BLACK;
        }
        /*
        else if ( gamePhase.getValue() == GamePhase.GREEN_FLAG )
        {
            dataBgColor = Color.GREEN;
            dataFontColor = Color.WHITE;
        }
        */
        else if ( gamePhase.getValue() == GamePhase.SESSION_STOPPED )
        {
            dataBgColor = Color.RED;
            dataFontColor = Color.WHITE;
        }
        
        if ( updateSessionLimit( gameData ) )
            changed = true;
        
        return ( changed );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory dsf, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        Texture2DCanvas texCanvas = texture.getTextureCanvas();
        texCanvas.setFont( getFont() );
        FontMetrics metrics = texCanvas.getFontMetrics();
        
        Rectangle2D capBounds = metrics.getStringBounds( caption, texCanvas );
        
        int dataAreaCenter = ETVUtils.getLabeledDataDataCenter( width, capBounds );
        int vMiddle = ETVUtils.getLabeledDataVMiddle( height, capBounds );
        
        captionString = dsf.newDrawnString( "captionString", ETVUtils.TRIANGLE_WIDTH, vMiddle, Alignment.LEFT, false, getFont(), isFontAntiAliased(), captionColor.getColor() );
        stateString = dsf.newDrawnString( "stateString", dataAreaCenter, vMiddle, Alignment.CENTER, false, getFont(), isFontAntiAliased(), getFontColor() );
        
        if ( sessionLimit == SessionLimit.LAPS )
            stateString.getMinColWidths( new String[] { "00", "/", "00" }, colAligns, colPadding, texture, colWidths );
        
        forceCompleteRedraw();
    }
    
    @Override
    protected void clearBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        ETVUtils.drawLabeledDataBackground( offsetX, offsetY, width, height, caption, getFont(), captionBackgroundColor.getColor(), dataBgColor, texture, true );
    }
    
    @Override
    public void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        final ScoringInfo scoringInfo = gameData.getScoringInfo();
        
        VehicleScoringInfo vsi = scoringInfo.getSessionType().isRace() ? scoringInfo.getVehicleScoringInfo( 0 ) : scoringInfo.getViewedVehicleScoringInfo();
        
        if ( needsCompleteRedraw )
        {
            captionString.draw( offsetX, offsetY, caption, captionBackgroundColor.getColor(), texture );
        }
        
        if ( sessionLimit == SessionLimit.TIME )
        {
            sessionTime.update( gameData.getScoringInfo().getSessionTime() );
            float endTime = gameData.getScoringInfo().getEndTime();
            if ( needsCompleteRedraw || ( clock1 && ( sessionTime.hasChanged( false ) || gamePhase.hasChanged( false ) ) ) )
            {
                sessionTime.setUnchanged();
                gamePhase.setUnchanged();
                
                if ( gamePhase.getValue() == GamePhase.SESSION_OVER )
                    stateString.draw( offsetX, offsetY, "00:00:00", dataBgColor, dataFontColor, texture );
                else if ( scoringInfo.getSessionType().isRace() && ( ( gamePhase.getValue() == GamePhase.FORMATION_LAP ) || ( endTime < 0f ) || ( endTime > 3000000f ) ) )
                    stateString.draw( offsetX, offsetY, "--:--:--", dataBgColor, dataFontColor, texture );
                else if ( scoringInfo.getSessionType().isTestDay() || ( endTime < 0f ) || ( endTime > 3000000f ) )
                    stateString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( sessionTime.getValue(), true, false ), dataBgColor, dataFontColor, texture );
                else
                    stateString.draw( offsetX, offsetY, TimingUtil.getTimeAsString( endTime - sessionTime.getValue(), true, false ), dataBgColor, dataFontColor, texture );
            }
        }
        else
        {
            if ( scoringInfo.getSessionType().isRace() && ( gamePhase.getValue() == GamePhase.FORMATION_LAP ) )
                lap.update( 0 );
            else if ( gameData.getProfileInfo().getShowCurrentLap() )
                lap.update( vsi.getCurrentLap() );
            else
                lap.update( vsi.getLapsCompleted() );
            
            if ( needsCompleteRedraw || ( clock1 && lap.hasChanged() ) )
            {
                int maxLaps = scoringInfo.getMaxLaps();
                String maxLapsStr = ( maxLaps < 10000 ) ? String.valueOf( maxLaps ) : "--";
                
                stateString.drawColumns( offsetX, offsetY, new String[] { lap.getValueAsString(), "/", maxLapsStr }, colAligns, colPadding, colWidths, dataBgColor, dataFontColor, texture );
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        super.saveProperties( writer );
        
        writer.writeProperty( sessionLimitPreference, "If a session is limited by both laps and time, this limit will be displayed." );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadProperty( String key, String value )
    {
        super.loadProperty( key, value );
        
        if ( sessionLimitPreference.loadProperty( key, value ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        super.getProperties( propsCont, forceAll );
        
        propsCont.addGroup( "Specific" );
        
        propsCont.addProperty( sessionLimitPreference );
    }
    
    public ETVSessionStateWidget( String name )
    {
        super( name, 12.0f, 2.54f );
    }
}

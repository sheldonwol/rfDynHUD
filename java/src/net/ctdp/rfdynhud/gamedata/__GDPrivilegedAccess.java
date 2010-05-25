package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.VehiclePhysics.Engine;
import net.ctdp.rfdynhud.input.InputAction;

public class __GDPrivilegedAccess
{
    public static final InputAction INPUT_ACTION_RESET_FUEL_CONSUMPTION = FuelUsageRecorder.INPUT_ACTION_RESET_FUEL_CONSUMPTION;
    public static final InputAction INPUT_ACTION_RESET_TOPSPEEDS = TopspeedRecorder.INPUT_ACTION_RESET_TOPSPEEDS;
    
    public static final void updateProfileInfo( ProfileInfo profileInfo )
    {
        profileInfo.update();
    }
    
    public static final void updateTrackInfo( TrackInfo trackInfo )
    {
        trackInfo.update();
    }
    
    public static final void updateInfo( LiveGameData gameData )
    {
        gameData.getProfileInfo().update();
        gameData.getModInfo().update();
        gameData.getTrackInfo().update();
        
        gameData.getPhysics().applyMeasurementUnits( gameData.getProfileInfo().getMeasurementUnits() );
    }
    
    public static final void loadEditorDefaults( VehiclePhysics physics )
    {
        physics.loadEditorDefaults();
    }
    
    public static final void loadFromPhysicsFiles( ProfileInfo profileInfo, TrackInfo trackInfo, VehiclePhysics physics )
    {
        physics.loadFromPhysicsFiles( profileInfo, trackInfo );
    }
    
    public static final boolean loadSetup( boolean isEditorMode, LiveGameData gameData )
    {
        return ( VehicleSetup.loadSetup( isEditorMode, gameData ) );
    }
    
    public static final void applyEditorPresets( EditorPresets editorPresets, LiveGameData gameData )
    {
        gameData.applyEditorPresets( editorPresets );
    }
    
    public static final LaptimesRecorder getLaptimesRecorder( ScoringInfo scoringInfo )
    {
        return ( scoringInfo.getLaptimesRecorder() );
    }
    
    public static final void loadFromStream( InputStream in, TelemetryData telemetryData ) throws IOException
    {
        telemetryData.loadFromStream( in );
    }
    
    public static final void loadFromStream( InputStream in, EditorPresets editorPresets, ScoringInfo scoringInfo ) throws IOException
    {
        scoringInfo.loadFromStream( in, editorPresets );
    }
    
    public static final void loadFromStream( InputStream in, CommentaryRequestInfo commentaryInfo ) throws IOException
    {
        commentaryInfo.loadFromStream( in );
    }
    
    public static final void loadFromStream( InputStream in, GraphicsInfo graphicsInfo ) throws IOException
    {
        graphicsInfo.loadFromStream( in );
    }
    
    public static final void onSessionStarted( LiveGameData gameData )
    {
        gameData.getTelemetryData().onSessionStarted();
        gameData.getScoringInfo().onSessionStarted();
    }
    
    public static final void setRealtimeMode( boolean realtimeMode, LiveGameData gameData )
    {
        gameData.setRealtimeMode( realtimeMode );
    }
    
    public static final void updateSessionTime( ScoringInfo scoringInfo, long timestamp )
    {
        scoringInfo.updateSessionTime( timestamp );
    }
    
    public static final void setEngineBoostMapping( int boost, TelemetryData telemData )
    {
        telemData.setEngineBoostMapping( boost );
    }
    
    public static final void incEngineBoostMapping( TelemetryData telemData, Engine engine )
    {
        telemData.incEngineBoostMapping( engine );
    }
    
    public static final void decEngineBoostMapping( TelemetryData telemData, Engine engine )
    {
        telemData.decEngineBoostMapping( engine );
    }
    
    public static final void setTempBoostFlag( TelemetryData telemData, boolean tempBoostFlag )
    {
        telemData.setTempBoostFlag( tempBoostFlag );
    }
    
    public static final void setTelemVect3( float x, float y, float z, TelemVect3 vect )
    {
        vect.x = x;
        vect.y = y;
        vect.z = z;
    }
}

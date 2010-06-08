package net.ctdp.rfdynhud.gamedata;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * 
 * @author Marvin Froehlich
 */
class VehicleScoringInfoCapsule
{
    private static final int OFFSET_DRIVER_NAME = 0;
    private static final int MAX_DRIVER_NAME_LENGTH = 32;
    private static final int OFFSET_VEHICLE_NAME = OFFSET_DRIVER_NAME + MAX_DRIVER_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int MAX_VEHICLE_NAME_LENGTH = 64;
    
    private static final int OFFSET_TOTAL_LAPS = OFFSET_VEHICLE_NAME + MAX_VEHICLE_NAME_LENGTH * ByteUtil.SIZE_CHAR;
    
    private static final int OFFSET_SECTOR = OFFSET_TOTAL_LAPS + ByteUtil.SIZE_SHORT;
    private static final int OFFSET_FINISH_STATUS = OFFSET_SECTOR + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LAP_DISTANCE = OFFSET_FINISH_STATUS + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_PATH_LATERAL = OFFSET_LAP_DISTANCE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TRACK_EDGE = OFFSET_PATH_LATERAL + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_BEST_SECTOR_1 = OFFSET_TRACK_EDGE + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_BEST_SECTOR_2 = OFFSET_BEST_SECTOR_1 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_BEST_LAP_TIME = OFFSET_BEST_SECTOR_2 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAST_SECTOR_1 = OFFSET_BEST_LAP_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAST_SECTOR_2 = OFFSET_LAST_SECTOR_1 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAST_LAP_TIME = OFFSET_LAST_SECTOR_2 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_CURR_SECTOR_1 = OFFSET_LAST_LAP_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_CURR_SECTOR_2 = OFFSET_CURR_SECTOR_1 + ByteUtil.SIZE_FLOAT;
    
    private static final int OFFSET_NUM_PITSTOPS = OFFSET_CURR_SECTOR_2 + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_NUM_PENALTIES = OFFSET_NUM_PITSTOPS + ByteUtil.SIZE_SHORT;
    
    private static final int OFFSET_IS_PLAYER = OFFSET_NUM_PENALTIES + ByteUtil.SIZE_SHORT;
    private static final int OFFSET_CONTROL = OFFSET_IS_PLAYER + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_IN_PITS = OFFSET_CONTROL + ByteUtil.SIZE_CHAR;
    private static final int OFFSET_PLACE = OFFSET_IN_PITS + ByteUtil.SIZE_BOOL;
    private static final int OFFSET_VEHICLE_CLASS = OFFSET_PLACE + ByteUtil.SIZE_CHAR;
    private static final int MAX_VEHICLE_CLASS_LENGTH = 32;
    
    private static final int OFFSET_TIME_BEHIND_NEXT = OFFSET_VEHICLE_CLASS + MAX_VEHICLE_CLASS_LENGTH * ByteUtil.SIZE_CHAR;
    private static final int OFFSET_LAPS_BEHIND_NEXT = OFFSET_TIME_BEHIND_NEXT + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_TIME_BEHIND_LEADER = OFFSET_LAPS_BEHIND_NEXT + ByteUtil.SIZE_LONG;
    private static final int OFFSET_LAPS_BEHIND_LEADER = OFFSET_TIME_BEHIND_LEADER + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LAP_START_TIME = OFFSET_LAPS_BEHIND_LEADER + ByteUtil.SIZE_LONG;
    
    private static final int OFFSET_POSITION = OFFSET_LAP_START_TIME + ByteUtil.SIZE_FLOAT;
    private static final int OFFSET_LOCAL_VELOCITY = OFFSET_POSITION + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_LOCAL_ACCELERATION = OFFSET_LOCAL_VELOCITY + ByteUtil.SIZE_VECTOR3;
    
    private static final int OFFSET_ORIENTATION_X = OFFSET_LOCAL_ACCELERATION + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_ORIENTATION_Y = OFFSET_ORIENTATION_X + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_ORIENTATION_Z = OFFSET_ORIENTATION_Y + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_LOCAL_ROTATION = OFFSET_ORIENTATION_Z + ByteUtil.SIZE_VECTOR3;
    private static final int OFFSET_LOCAL_ROTATION_ACCELERATION = OFFSET_LOCAL_ROTATION + ByteUtil.SIZE_VECTOR3;
    
    private static final int OFFSET_EXPANSION = OFFSET_LOCAL_ROTATION_ACCELERATION + ByteUtil.SIZE_VECTOR3;
    
    static final int BUFFER_SIZE = OFFSET_EXPANSION + 128 * ByteUtil.SIZE_CHAR;
    
    private final byte[] buffer = new byte[ BUFFER_SIZE ];
    
    private int hash = 0;
    private boolean hasHash = false;
    
    private static final HashMap<Object, Integer> idMap = new HashMap<Object, Integer>();
    private static int nextId = 1;
    private Integer id = null;
    
    private static class HashItem
    {
        private final byte[] buffer = new byte[ MAX_DRIVER_NAME_LENGTH ];
        private final int hash;
        
        @Override
        public int hashCode()
        {
            return ( hash );
        }
        
        @Override
        public boolean equals( Object o )
        {
            byte[] buffer2;
            int offset2;
            if ( o instanceof VehicleScoringInfoCapsule )
            {
                buffer2 = ( (VehicleScoringInfoCapsule)o ).buffer;
                offset2 = OFFSET_DRIVER_NAME;
            }
            else if ( o instanceof HashItem )
            {
                buffer2 = ( (HashItem)o ).buffer;
                offset2 = 0;
            }
            else
            {
                return ( false );
            }
            
            for ( int i = 0; i < MAX_DRIVER_NAME_LENGTH; i++ )
            {
                byte ch1 = this.buffer[i];
                byte ch2 = buffer2[offset2 + i];
                
                if ( ch1 != ch2 )
                    return ( false );
                
                if ( ch1 == (byte)0 )
                    break;
            }
            
            return ( true );
        }
        
        public HashItem( VehicleScoringInfoCapsule vsic )
        {
            System.arraycopy( vsic.buffer, OFFSET_DRIVER_NAME, this.buffer, 0, MAX_DRIVER_NAME_LENGTH );
            this.hash = vsic.hashCode();
        }
    }
    
    byte[] getBuffer()
    {
        hasHash = false;
        
        return ( buffer );
    }
    
    Integer refreshID()
    {
        id = idMap.get( this );
        if ( id == null )
        {
            id = nextId++;
            idMap.put( new HashItem( this ), id );
        }
        
        return ( id );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        if ( !hasHash )
        {
            hash = 0;
            
            for ( int i = 0; i < MAX_DRIVER_NAME_LENGTH; i++ )
            {
                int ch = buffer[OFFSET_DRIVER_NAME + i] & 0xFF;
                if ( ch == 0 )
                    break;
                
                hash = 31 * hash + ch;
            }
            
            hasHash = true;
        }
        
        return ( hash );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals( Object o )
    {
        byte[] buffer2;
        int offset2;
        if ( o instanceof VehicleScoringInfoCapsule )
        {
            buffer2 = ( (VehicleScoringInfoCapsule)o ).buffer;
            offset2 = OFFSET_DRIVER_NAME;
        }
        else if ( o instanceof HashItem )
        {
            buffer2 = ( (HashItem)o ).buffer;
            offset2 = 0;
        }
        else
        {
            return ( false );
        }
        
        for ( int i = 0; i < MAX_DRIVER_NAME_LENGTH; i++ )
        {
            byte ch1 = this.buffer[OFFSET_DRIVER_NAME + i];
            byte ch2 = buffer2[offset2 + i];
            
            if ( ch1 != ch2 )
                return ( false );
            
            if ( ch1 == (byte)0 )
                break;
        }
        
        return ( true );
    }
    
    void loadFromStream( InputStream in ) throws IOException
    {
        hasHash = false;
        
        int offset = 0;
        int bytesToRead = BUFFER_SIZE;
        
        while ( bytesToRead > 0 )
        {
            int n = in.read( buffer, offset, bytesToRead );
            
            if ( n < 0 )
                throw new IOException();
            
            offset += n;
            bytesToRead -= n;
        }
    }
    
    /*
     * ################################
     * VehicleScoringInfo
     * ################################
     */
    
    void setDriverName( String drivername )
    {
        byte[] bytes = drivername.getBytes();
        System.arraycopy( bytes, 0, buffer, OFFSET_DRIVER_NAME, bytes.length );
        buffer[OFFSET_DRIVER_NAME + bytes.length] = (byte)0;
    }
    
    int postfixDriverName( String postfix, int pos )
    {
        if ( pos < 0 )
        {
            for ( int i = 0; i < MAX_DRIVER_NAME_LENGTH; i++ )
            {
                if ( buffer[OFFSET_DRIVER_NAME + i] == (byte)0 )
                {
                    pos = i;
                    
                    break;
                }
            }
        }
        
        if ( pos >= 0 )
        {
            byte[] bytes = postfix.getBytes();
            System.arraycopy( bytes, 0, buffer, OFFSET_DRIVER_NAME + pos, bytes.length );
            buffer[OFFSET_DRIVER_NAME + pos + postfix.length()] = (byte)0;
        }
        
        return ( pos );
    }
    
    public final String getDriverName()
    {
        // char mDriverName[32]
        
        return ( ByteUtil.readString( buffer, OFFSET_DRIVER_NAME, MAX_DRIVER_NAME_LENGTH ) );
    }
    
    /**
     * vehicle name
     */
    public final String getVehicleName()
    {
        // char mVehicleName[64]
        
        return ( ByteUtil.readString( buffer, OFFSET_VEHICLE_NAME, MAX_VEHICLE_NAME_LENGTH ) );
    }
    
    /**
     * laps completed
     */
    public final short getLapsCompleted()
    {
        // short mTotalLaps
        
        return ( ByteUtil.readShort( buffer, OFFSET_TOTAL_LAPS ) );
    }
    
    /**
     * sector
     */
    public final byte getSector()
    {
        // signed char mSector
        
        byte sector = (byte)( ByteUtil.readByte( buffer, OFFSET_SECTOR ) + 1 );
        
        if ( sector == 1 )
            return ( 3 );
        
        return ( (byte)( sector - 1 ) );
    }
    
    /**
     * finish status
     */
    public final FinishStatus getFinishStatus()
    {
        // signed char mFinishStatus
        
        short state = ByteUtil.readByte( buffer, OFFSET_FINISH_STATUS );
        
        switch ( state )
        {
            case 0:
                return ( FinishStatus.NONE );
            case 1:
                return ( FinishStatus.FINISHED );
            case 2:
                return ( FinishStatus.DNF );
            case 3:
                return ( FinishStatus.DQ );
        }
        
        throw new Error( "Unknown finish status read (" + state + ")." );
    }
    
    /**
     * current distance around track
     */
    public final float getLapDistance()
    {
        // float mLapDist
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAP_DISTANCE ) );
    }
    
    /**
     * lateral position with respect to *very approximate* "center" path
     */
    public final float getPathLateral()
    {
        // float mPathLateral
        
        return ( ByteUtil.readFloat( buffer, OFFSET_PATH_LATERAL ) );
    }
    
    /**
     * track edge (w.r.t. "center" path) on same side of track as vehicle
     */
    public final float getTrackEdge()
    {
        // float mTrackEdge
        
        return ( ByteUtil.readFloat( buffer, OFFSET_TRACK_EDGE ) );
    }
    
    /**
     * best sector 1
     */
    public final float getBestSector1()
    {
        // float mBestSector1
        
        return ( ByteUtil.readFloat( buffer, OFFSET_BEST_SECTOR_1 ) );
    }
    
    /**
     * best sector 2
     */
    public final float getBestSector2()
    {
        // float mBestSector2
        
        return ( ByteUtil.readFloat( buffer, OFFSET_BEST_SECTOR_2 ) );
    }
    
    /**
     * best lap time
     */
    public final float getBestLapTime()
    {
        // float mBestLapTime
        
        return ( ByteUtil.readFloat( buffer, OFFSET_BEST_LAP_TIME ) );
    }
    
    /**
     * last sector 1
     */
    public final float getLastSector1()
    {
        // float mLastSector1
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAST_SECTOR_1 ) );
    }
    
    /**
     * last sector 2
     */
    public final float getLastSector2()
    {
        // float mLastSector2
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAST_SECTOR_2 ) );
    }
    
    /**
     * last lap time
     */
    public final float getLastLapTime()
    {
        // float mLastLapTime
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAST_LAP_TIME ) );
    }
    
    /**
     * current sector 1 (if valid)
     */
    public final float getCurrentSector1()
    {
        // float mCurSector1
        
        // TODO: Check result, if sector1 is invalid
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CURR_SECTOR_1 ) );
    }
    
    /**
     * current sector 2
     * 
     * @param includingSector1 only affects result if sector1 is valid
     */
    public final float getCurrentSector2()
    {
        // float mCurSector2
        
        return ( ByteUtil.readFloat( buffer, OFFSET_CURR_SECTOR_2 ) );
    }
    
    /**
     * number of pitstops made
     */
    public final short getNumPitstopsMade()
    {
        // short mNumPitstops
        
        return ( ByteUtil.readShort( buffer, OFFSET_NUM_PITSTOPS ) );
    }
    
    /**
     * number of outstanding penalties
     */
    public final short getNumOutstandingPenalties()
    {
        // short mNumPenalties
        
        return ( ByteUtil.readShort( buffer, OFFSET_NUM_PENALTIES ) );
    }
    
    /*
     * ################################
     * VehicleScoringInfoV2
     * ################################
     */
    
    /**
     * is this the player's vehicle?
     */
    public final boolean isPlayer()
    {
        // bool mIsPlayer
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_IS_PLAYER ) );
    }
    
    /**
     * who's in control?
     */
    public final VehicleControl getVehicleControl()
    {
        // signed char mControl
        
        byte control = ByteUtil.readByte( buffer, OFFSET_CONTROL );
        VehicleControl vc = VehicleControl.getFromISIValue( control );
        
        if ( vc == null )
            throw new Error( "Unknown control id read (" + control + ")." );
        
        return ( vc );
    }
    
    /**
     * between pit entrance and pit exit (not always accurate for remote vehicles)
     */
    public final boolean isInPits()
    {
        // bool mInPits
        
        return ( ByteUtil.readBoolean( buffer, OFFSET_IN_PITS ) );
    }
    
    /**
     * 1-based position
     */
    public final short getPlace()
    {
        // unsigned char mPlace
        
        return ( ByteUtil.readUnsignedByte( buffer, OFFSET_PLACE ) );
    }
    
    /**
     * vehicle class
     */
    public final String getVehicleClass()
    {
        // char mVehicleClass[32]
        
        return ( ByteUtil.readString( buffer, OFFSET_VEHICLE_CLASS, MAX_VEHICLE_CLASS_LENGTH ) );
    }
    
    /**
     * time behind vehicle in next higher place
     */
    public final float getTimeBehindNextInFront()
    {
        // float mTimeBehindNext
        
        return ( ByteUtil.readFloat( buffer, OFFSET_TIME_BEHIND_NEXT ) );
    }
    
    /**
     * laps behind vehicle in next higher place
     */
    public final int getLapsBehindNextInFront()
    {
        // long mLapsBehindNext
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_LAPS_BEHIND_NEXT ) );
    }
    
    /**
     * time behind leader
     */
    public final float getTimeBehindLeader()
    {
        // float mTimeBehindLeader
        
        return ( ByteUtil.readFloat( buffer, OFFSET_TIME_BEHIND_LEADER ) );
    }
    
    /**
     * laps behind leader
     */
    public final int getLapsBehindLeader()
    {
        // long mLapsBehindLeader
        
        return ( (int)ByteUtil.readLong( buffer, OFFSET_LAPS_BEHIND_LEADER ) );
    }
    
    /**
     * time this lap was started
     */
    public final float getLapStartTime()
    {
        // float mLapStartET
        
        return ( ByteUtil.readFloat( buffer, OFFSET_LAP_START_TIME ) );
    }
    
    // Position and derivatives
    
    /**
     * world position in meters
     * 
     * @param position
     */
    public final void getWorldPosition( TelemVect3 position )
    {
        // TelemVect3 mPos
        
        ByteUtil.readVector( buffer, OFFSET_POSITION, position );
    }
    
    /**
     * world position in meters
     */
    public final float getWorldPositionX()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_POSITION + 0 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * world position in meters
     */
    public final float getWorldPositionY()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_POSITION + 1 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * world position in meters
     */
    public final float getWorldPositionZ()
    {
        // TelemVect3 mPos
        
        return ( ByteUtil.readFloat( buffer, OFFSET_POSITION + 2 * ByteUtil.SIZE_FLOAT ) );
    }
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     * 
     * @param localVel
     */
    public final void getLocalVelocity( TelemVect3 localVel )
    {
        // TelemVect3 mLocalVel
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_VELOCITY, localVel );
    }
    
    /**
     * velocity (meters/sec) in local vehicle coordinates
     */
    public final float getScalarVelocity()
    {
        float vecX = ByteUtil.readFloat( buffer, OFFSET_LOCAL_VELOCITY + 0 * ByteUtil.SIZE_FLOAT );
        float vecY = ByteUtil.readFloat( buffer, OFFSET_LOCAL_VELOCITY + 1 * ByteUtil.SIZE_FLOAT );
        float vecZ = ByteUtil.readFloat( buffer, OFFSET_LOCAL_VELOCITY + 2 * ByteUtil.SIZE_FLOAT );
        
        return ( (float)Math.sqrt( vecX * vecX + vecY * vecY + vecZ * vecZ ) );
    }
    
    /**
     * acceleration (meters/sec^2) in local vehicle coordinates
     * 
     * @param localAccel
     */
    public final void getLocalAcceleration( TelemVect3 localAccel )
    {
        // TelemVect3 mLocalAccel
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_ACCELERATION, localAccel );
    }
    
    // Orientation and derivatives
    
    /**
     * top row of orientation matrix (also converts local vehicle vectors into world X using dot product)
     * 
     * @param oriX
     */
    public final void getOrientationX( TelemVect3 oriX )
    {
        // TelemVect3 mOriX
        
        ByteUtil.readVector( buffer, OFFSET_ORIENTATION_X, oriX );
    }
    
    /**
     * mid row of orientation matrix (also converts local vehicle vectors into world Y using dot product)
     * 
     * @param oriY
     */
    public final void getOrientationY( TelemVect3 oriY )
    {
        // TelemVect3 mOriY
        
        ByteUtil.readVector( buffer, OFFSET_ORIENTATION_Y, oriY );
    }
    
    /**
     * bot row of orientation matrix (also converts local vehicle vectors into world Z using dot product)
     * 
     * @param oriZ
     */
    public final void getOrientationZ( TelemVect3 oriZ )
    {
        // TelemVect3 mOriZ
        
        ByteUtil.readVector( buffer, OFFSET_ORIENTATION_Z, oriZ );
    }
    
    /**
     * rotation (radians/sec) in local vehicle coordinates
     * 
     * @param localRot
     */
    public final void getLocalRotation( TelemVect3 localRot )
    {
        // TelemVect3 mLocalRot
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_ROTATION, localRot );
    }
    
    /**
     * rotational acceleration (radians/sec^2) in local vehicle coordinates
     * 
     * @param localRotAccel
     */
    public final void getLocalRotationalAcceleration( TelemVect3 localRotAccel )
    {
        // TelemVect3 mLocalRotAccel
        
        ByteUtil.readVector( buffer, OFFSET_LOCAL_ROTATION_ACCELERATION, localRotAccel );
    }
    
    // Future use
    //unsigned char mExpansion[128];
}
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
package net.ctdp.rfdynhud.util;

import java.io.File;
import java.io.IOException;

import net.ctdp.rfdynhud.gamedata.GameFileSystem;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;

/**
 * Don't use this at home!
 * 
 * @author Marvin Froehlich (CTDP)
 */
public class __UtilPrivilegedAccess
{
    public static void updateLocalizationsManager( GameFileSystem fileSystem )
    {
        LocalizationsManager.INSTANCE.update( fileSystem );
    }
    
    public static File forceLoadConfiguration( ConfigurationLoader loader, File file, final WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode ) throws IOException
    {
        return ( loader.forceLoadConfiguration( file, widgetsConfig, gameData, isEditorMode ) );
    }
    
    public static void loadFactoryDefaults( ConfigurationLoader loader, WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode ) throws IOException
    {
        loader.loadFactoryDefaults( widgetsConfig, gameData, isEditorMode );
    }
    
    public static void reloadConfiguration( ConfigurationLoader loader, File configFolder, boolean smallMonitor, boolean bigMonitor, boolean isInGarage, String modName, String vehicleClass, String vehicleName, SessionType sessionType, WidgetsConfiguration widgetsConfig, LiveGameData gameData, boolean isEditorMode, boolean force )
    {
        loader.reloadConfiguration( configFolder, smallMonitor, bigMonitor, isInGarage, modName, vehicleClass, vehicleName, sessionType, widgetsConfig, gameData, isEditorMode, force );
    }
    
    public static final TransformableTexture[] getSubTextureArray( SubTextureCollector collector, boolean sort )
    {
        return ( collector.getArray( sort ) );
    }
}

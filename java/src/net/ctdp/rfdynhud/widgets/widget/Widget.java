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
package net.ctdp.rfdynhud.widgets.widget;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;

import net.ctdp.rfdynhud.editor.EditorPresets;
import net.ctdp.rfdynhud.gamedata.LiveGameData;
import net.ctdp.rfdynhud.gamedata.ScoringInfo;
import net.ctdp.rfdynhud.gamedata.SessionType;
import net.ctdp.rfdynhud.gamedata.VehicleScoringInfo;
import net.ctdp.rfdynhud.gamedata.VehicleSetup;
import net.ctdp.rfdynhud.input.InputAction;
import net.ctdp.rfdynhud.properties.BackgroundProperty;
import net.ctdp.rfdynhud.properties.BooleanProperty;
import net.ctdp.rfdynhud.properties.BorderProperty;
import net.ctdp.rfdynhud.properties.ColorProperty;
import net.ctdp.rfdynhud.properties.FontProperty;
import net.ctdp.rfdynhud.properties.IntProperty;
import net.ctdp.rfdynhud.properties.Property;
import net.ctdp.rfdynhud.properties.PropertyLoader;
import net.ctdp.rfdynhud.properties.StringProperty;
import net.ctdp.rfdynhud.properties.WidgetPropertiesContainer;
import net.ctdp.rfdynhud.render.BorderWrapper;
import net.ctdp.rfdynhud.render.DrawnString;
import net.ctdp.rfdynhud.render.DrawnStringFactory;
import net.ctdp.rfdynhud.render.Texture2DCanvas;
import net.ctdp.rfdynhud.render.TextureImage2D;
import net.ctdp.rfdynhud.render.TransformableTexture;
import net.ctdp.rfdynhud.render.__RenderPrivilegedAccess;
import net.ctdp.rfdynhud.util.Documented;
import net.ctdp.rfdynhud.util.Logger;
import net.ctdp.rfdynhud.util.StringUtil;
import net.ctdp.rfdynhud.util.WidgetsConfigurationWriter;
import net.ctdp.rfdynhud.values.InnerSize;
import net.ctdp.rfdynhud.values.Position;
import net.ctdp.rfdynhud.values.RelativePositioning;
import net.ctdp.rfdynhud.values.Size;
import net.ctdp.rfdynhud.widgets.WidgetsConfiguration;
import net.ctdp.rfdynhud.widgets.__WCPrivilegedAccess;

import org.jagatoo.util.classes.ClassUtil;
import org.openmali.types.twodee.Rect2i;

/**
 * This is the base for all Widgets to be drawn on the HUD.<br>
 * Any concrete extension must have a parameterless constructor.
 * 
 * @author Marvin Froehlich (CTDP)
 */
public abstract class Widget implements Documented
{
    public static final int NEEDED_DATA_TELEMETRY = 1;
    public static final int NEEDED_DATA_SCORING = 2;
    //public static final int NEEDED_DATA_SETUP = 4;
    public static final int NEEDED_DATA_ALL = NEEDED_DATA_TELEMETRY | NEEDED_DATA_SCORING/* | NEEDED_DATA_SETUP*/;
    
    private WidgetsConfiguration config = null;
    
    private boolean dirtyFlag = true;
    
    private final StringProperty type = new StringProperty( this, "type", Widget.this.getClass().getSimpleName(), true );
    
    private final StringProperty name = new StringProperty( this, "name", "" );
    
    private final Position position;
    private final Size size;
    private final InnerSize innerSize;
    
    /**
     * Gets the initial value for the background property.
     * 
     * @return the initial value for the background property.
     */
    protected String getInitialBackground()
    {
        return ( BackgroundProperty.COLOR_INDICATOR + ColorProperty.STANDARD_BACKGROUND_COLOR_NAME );
    }
    
    final boolean overridesDrawBackground = ClassUtil.overridesMethod( Widget.class, this.getClass(), "drawBackground", LiveGameData.class, EditorPresets.class, TextureImage2D.class, int.class, int.class, int.class, int.class, boolean.class );
    
    /**
     * This method is invoked when the background has changed.
     * 
     * @param deltaScaleX the x-scale factor in as a difference between the old background image and the new one or -1 of no background image was selected
     * @param deltaScaleY the y-scale factor in as a difference between the old background image and the new one or -1 of no background image was selected
     */
    protected void onBackgroundChanged( float deltaScaleX, float deltaScaleY ) {}
    private final BackgroundProperty backgroundProperty = canHaveBackground() || overridesDrawBackground ? new BackgroundProperty( this, "background", getInitialBackground() )
    {
        @Override
        protected void onValueChanged( BackgroundType oldBGType, BackgroundType newBGType, String oldValue, String newValue )
        {
            if ( ( background != null ) && ( getConfiguration() != null ) )
                background.onPropertyValueChanged( Widget.this, oldBGType, newBGType, oldValue, newValue );
        }
    } : null;
    private final WidgetBackground background = canHaveBackground() || overridesDrawBackground ? new WidgetBackground( this, backgroundProperty ) : null;
    
    private final FontProperty font = new FontProperty( this, "font", FontProperty.STANDARD_FONT_NAME );
    private final ColorProperty fontColor = new ColorProperty( this, "fontColor", ColorProperty.STANDARD_FONT_COLOR_NAME );
    
    private final IntProperty paddingTop = new IntProperty( this, "paddingTop", "top", 0, 0, 1000, false );
    private final IntProperty paddingLeft = new IntProperty( this, "paddingLeft", "left", 0, 0, 1000, false );
    private final IntProperty paddingRight = new IntProperty( this, "paddingRight", "right", 0, 0, 1000, false );
    private final IntProperty paddingBottom = new IntProperty( this, "paddingBottom", "bottom", 0, 0, 1000, false );
    
    private final BorderProperty border = new BorderProperty( this, "border", BorderProperty.DEFAULT_BORDER_NAME, paddingTop, paddingLeft, paddingRight, paddingBottom );
    
    private final BooleanProperty inputVisible = new BooleanProperty( this, "initialVisibility", true );
    private boolean userVisible1 = true;
    private boolean userVisible2 = true;
    private boolean visibilityChangedSinceLastDraw = true;
    private boolean needsCompleteRedraw = true;
    private boolean needsCompleteClear = false;
    
    private boolean initialized = false;
    
    private TransformableTexture[] subTextures = null;
    
    private final DrawnStringFactory drawnStringFactory = new DrawnStringFactory( this );
    
    private AssembledWidget masterWidget = null;
    
    /**
     * Logs data to the plugin's log file.
     * 
     * @param data
     */
    protected final void log( Object... data )
    {
        if ( ( data == null ) || ( data.length == 0 ) )
            return;
        
        for ( int i = 0; i < data.length; i++ )
        {
            Logger.log( data[i], i == data.length - 1 );
        }
    }
    
    protected static final int composeVersion( int major, int minor, int revision )
    {
        major = ( ( major + 1 ) & 0xFF ) << 23; // 8 bits for major (max 255)
        minor = ( ( minor + 1 ) & 0x400 ) << 13; // 10 bits for minor (max 1023)
        revision = ( revision & 0x2000 ) << 0; // 13 bits for revision (max 8191)
        
        return ( major | minor | revision );
    }
    
    /**
     * Gets a comparable version indicator for this {@link Widget}.
     * 
     * @return a comparable version indicator for this {@link Widget}.
     */
    public abstract int getVersion();
    
    public final String getVersionString()
    {
        int version = getVersion();
        
        if ( version <= 0x7FFFFF ) // > 2^23-1
            return ( String.valueOf( version ) );
        
        int major = ( ( version >>> 23 ) - 1 ) & 0xFF;
        int minor = ( ( version >>> 13 ) - 1 ) & 0x400;
        int revision = ( version >>> 0 ) & 0x2000;
        
        return ( major + "." + minor + "." + revision );
    }
    
    protected void onVisibilityChanged( boolean visible )
    {
        if ( visible )
            this.needsCompleteRedraw = true;
        else
            this.needsCompleteClear = true;
        
        this.visibilityChangedSinceLastDraw = true;
    }
    
    /**
     * 
     * @param property
     * @param oldValue
     * @param newValue
     */
    protected void onPropertyChanged( Property property, Object oldValue, Object newValue )
    {
        forceCompleteRedraw( true );
    }
    
    /**
     * 
     * @param oldPositioning
     * @param oldX
     * @param oldY
     * @param newPositioning
     * @param newX
     * @param newY
     */
    protected void onPositionChanged( RelativePositioning oldPositioning, int oldX, int oldY, RelativePositioning newPositioning, int newX, int newY )
    {
        WidgetsConfiguration wc = getConfiguration();
        
        if ( wc != null )
        {
            __WCPrivilegedAccess.sortWidgets( wc );
        }
    }
    
    /**
     * 
     * @param oldWidth
     * @param oldHeight
     * @param newWidth
     * @param newHeight
     */
    protected void onSizeChanged( int oldWidth, int oldHeight, int newWidth, int newHeight )
    {
        WidgetsConfiguration wc = getConfiguration();
        
        if ( wc != null )
        {
            __WCPrivilegedAccess.sortWidgets( wc );
        }
        
        if ( getBackground() != null )
        {
            getBackground().onWidgetSizeChanged( this );
        }
    }
    
    /**
     * Gets the default value for the given border alias/name.
     * 
     * @param name
     * 
     * @return the default value for the given border alias/name.
     */
    public String getDefaultBorderValue( String name )
    {
        return ( BorderProperty.getDefaultBorderValue( name ) );
    }
    
    /**
     * Gets the default value for the given named color.
     * 
     * @param name
     * 
     * @return the default value for the given named color.
     */
    public String getDefaultNamedColorValue( String name )
    {
        return ( ColorProperty.getDefaultNamedColorValue( name ) );
    }
    
    /**
     * Gets the default value for the given named font.
     * 
     * @param name
     * 
     * @return the default value for the given named font.
     */
    public String getDefaultNamedFontValue( String name )
    {
        return ( FontProperty.getDefaultNamedFontValue( name ) );
    }
    
    /**
     * Gets the package to group the Widget in the editor.
     * This can be an <code>null</code> to be displayed in the root or a slash separated path.
     * 
     * @return the package to group the Widget in the editor.
     */
    public abstract WidgetPackage getWidgetPackage();
    
    void setConfiguration( WidgetsConfiguration config )
    {
        this.config = config;
    }
    
    /**
     * Gets the {@link WidgetsConfiguration}, this {@link Widget} is a member of.
     * 
     * @return the {@link WidgetsConfiguration}, this {@link Widget} is a member of.
     */
    public final WidgetsConfiguration getConfiguration()
    {
        return ( config );
    }
    
    void setMasterWidget( AssembledWidget masterWidget )
    {
        this.masterWidget = masterWidget;
    }
    
    /**
     * If this {@link Widget} is part of an {@link AssembledWidget}, this master {@link Widget} is returned.
     * 
     * @return the master {@link AssembledWidget} or <code>null</code>.
     */
    public final AssembledWidget getMasterWidget()
    {
        return ( masterWidget );
    }
    
    /**
     * Gets the InputActions, that can be bound with a Widget of this type.
     * "Override" this method to return your own custom actions.
     * 
     * @return the InputActions, that can be bound with a Widget of this type.
     */
    public InputAction[] getInputActions()
    {
        return ( null );
    }
    
    /**
     * Gets the {@link TransformableTexture}s, that this {@link Widget} keeps.
     * 
     * @param gameData
     * @param editorPresets
     * @param widgetInnerWidth
     * @param widgetInnerHeight
     * 
     * @return the {@link TransformableTexture}s, that this {@link Widget} keeps or null for no textures.
     */
    protected TransformableTexture[] getSubTexturesImpl( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        return ( null );
    }
    
    /**
     * Gets the {@link TransformableTexture}s, that this {@link Widget} keeps.
     * 
     * @param gameData
     * @param editorPresets
     * @param widgetInnerHeight
     * @param widgetInnerWidth
     * 
     * @return the {@link TransformableTexture}s, that this {@link Widget} keeps or null for no textures.
     */
    public final TransformableTexture[] getSubTextures( LiveGameData gameData, EditorPresets editorPresets, int widgetInnerWidth, int widgetInnerHeight )
    {
        if ( !initialized )
        {
            subTextures = getSubTexturesImpl( gameData, editorPresets, widgetInnerWidth, widgetInnerHeight );
            
            if ( subTextures != null )
            {
                for ( int i = 0; i < subTextures.length; i++ )
                {
                    if ( subTextures[i].getOwnerWidget() == null )
                        __RenderPrivilegedAccess.setOwnerWidget( this, subTextures[i] );
                }
            }
        }
        
        return ( subTextures );
    }
    
    protected void onDirtyFlagSet()
    {
    }
    
    void setDirtyFlag( boolean forwardCall )
    {
        boolean changed = !this.dirtyFlag;
        
        this.dirtyFlag = true;
        
        if ( forwardCall && ( masterWidget != null ) )
            masterWidget.setDirtyFlag( false );
        
        if ( changed )
            onDirtyFlagSet();
    }
    
    public void setDirtyFlag()
    {
        this.dirtyFlag = true;
        
        if ( masterWidget != null )
            masterWidget.setDirtyFlag();
    }
    
    public final boolean getDirtyFlag( boolean reset )
    {
        boolean result = dirtyFlag;
        
        if ( reset )
            this.dirtyFlag = false;
        
        return ( result );
    }
    
    protected void onReinitializationForced()
    {
    }
    
    final boolean isInitialized()
    {
        return ( initialized );
    }
    
    void forceReinitialization( boolean forwardCall )
    {
        boolean changed = this.initialized;
        
        this.initialized = false;
        
        if ( forwardCall && ( masterWidget != null ) )
            masterWidget.forceReinitialization( false );
        
        setDirtyFlag();
        
        if ( changed )
            onReinitializationForced();
    }
    
    public final void forceReinitialization()
    {
        forceReinitialization( true );
    }
    
    /**
     * Sets this {@link Widget}'s name.
     * 
     * @param name
     */
    public void setName( String name )
    {
        this.name.setStringValue( name );
        setDirtyFlag();
    }
    
    /**
     * Gets this {@link Widget}'s name.
     * 
     * @return this {@link Widget}'s name.
     */
    public final String getName()
    {
        return ( name.getStringValue() );
    }
    
    /**
     * Gets the {@link Widget}'s position.
     * 
     * @return the {@link Widget}'s position.
     */
    public final Position getPosition()
    {
        return ( position );
    }
    
    /**
     * Gets the x-offset relative to the master Widget.
     * 
     * @return the x-offset relative to the master Widget.
     */
    public final int getOffsetXToMasterWidget()
    {
        if ( getMasterWidget() == null )
            return ( 0 );
        
        return ( position.getEffectiveX() + getMasterWidget().getOffsetXToMasterWidget() );
    }
    
    /**
     * Gets the y-offset relative to the master Widget.
     * 
     * @return the y-offset relative to the master Widget.
     */
    public final int getOffsetYToMasterWidget()
    {
        if ( getMasterWidget() == null )
            return ( 0 );
        
        return ( position.getEffectiveY() + getMasterWidget().getOffsetYToMasterWidget() );
    }
    
    /**
     * Gets, whether this {@link Widget} has a fixed (unmodifiable) size.
     * 
     * @return whether this {@link Widget} has a fixed (unmodifiable) size.
     */
    public boolean hasFixedSize()
    {
        return ( false );
    }
    
    /**
     * Gets this {@link Widget}'s size.
     * 
     * @return this {@link Widget}'s width.
     */
    public final Size getSize()
    {
        return ( size );
    }
    
    /**
     * Gets the inner size of the {@link Widget}.
     * 
     * @return the inner size of the {@link Widget}.
     */
    public final InnerSize getInnerSize()
    {
        return ( innerSize );
    }
    
    /**
     * Gets the result of getSize().getEffectiveWidth().
     * 
     * @return the result of getSize().getEffectiveWidth().
     */
    public final int getEffectiveWidth()
    {
        return ( size.getEffectiveWidth() );
    }
    
    /**
     * Gets the result of getSize().getEffectiveHeight().
     * 
     * @return the result of getSize().getEffectiveHeight().
     */
    public final int getEffectiveHeight()
    {
        return ( size.getEffectiveHeight() );
    }
    
    /**
     * Gets the minimum width for this {@link Widget} in pixels.
     * 
     * @param gameData
     * @param editorPresets
     * 
     * @return the minimum width for this {@link Widget} in pixels.
     */
    public int getMinWidth( LiveGameData gameData, EditorPresets editorPresets )
    {
        return ( 25 );
    }
    
    /**
     * Gets the minimum height for this {@link Widget} in pixels.
     * 
     * @param gameData
     * @param editorPresets
     * 
     * @return the minimum height for this {@link Widget} in pixels.
     */
    public int getMinHeight( LiveGameData gameData, EditorPresets editorPresets )
    {
        return ( 25 );
    }
    
    /**
     * Gets the maximum width covered by this {@link Widget}.
     * By default this method returns the result of getEffectiveWidth(gameResX).
     * Override this method, if it will change its size during game play.
     * 
     * @param gameData
     * @param editorPresets
     * @param texture
     * 
     * @return the maximum width covered by this {@link Widget}.
     */
    public int getMaxWidth( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture )
    {
        return ( size.getEffectiveWidth() );
    }
    
    /**
     * Gets the maximum height covered by this {@link Widget}.
     * By default this method returns the result of getEffectiveHeight(gameResX).
     * Override this method, if it will change its size during game play.
     * 
     * @param gameData
     * @param editorPresets
     * @param texture
     * 
     * @return the maximum height covered by this {@link Widget}.
     */
    public int getMaxHeight( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture )
    {
        return ( size.getEffectiveHeight() );
    }
    
    /**
     * Bakes effective position and size to variables, so that they don't need to be recalculated
     * during runtime on each access.
     */
    public void bake()
    {
        position.bake();
        size.bake();
    }
    
    public void setAllPosAndSizeToPercents()
    {
        position.setXToPercents();
        position.setYToPercents();
        
        size.setWidthToPercents();
        size.setHeightToPercents();
    }
    
    public void setAllPosAndSizeToPixels()
    {
        position.setXToPixels();
        position.setYToPixels();
        
        size.setWidthToPixels();
        size.setHeightToPixels();
    }
    
    public final BackgroundProperty getBackgroundProperty()
    {
        return ( backgroundProperty );
    }
    
    /**
     * Gets the {@link Widget}'s background.
     * 
     * @return the {@link Widget}'s background.
     */
    public final WidgetBackground getBackground()
    {
        return ( background );
    }
    
    public final FontProperty getFontProperty()
    {
        return ( font );
    }
    
    protected final java.awt.Font getFont()
    {
        return ( font.getFont() );
    }
    
    protected final boolean isFontAntiAliased()
    {
        return ( font.isAntiAliased() );
    }
    
    public final ColorProperty getFontColorProperty()
    {
        return ( fontColor );
    }
    
    /**
     * Gets the {@link Widget}'s font color.
     * 
     * @return the {@link Widget}'s font color.
     */
    protected final Color getFontColor()
    {
        return ( fontColor.getColor() );
    }
    
    protected final IntProperty getPaddingPropertyTop()
    {
        return ( paddingTop );
    }
    
    protected final IntProperty getPaddingPropertyLeft()
    {
        return ( paddingLeft );
    }
    
    protected final IntProperty getPaddingPropertyRight()
    {
        return ( paddingRight );
    }
    
    protected final IntProperty getPaddingPropertyBottom()
    {
        return ( paddingBottom );
    }
    
    protected final BorderProperty getBorderProperty()
    {
        return ( border );
    }
    
    /**
     * Sets padding for this Widget.
     * 
     * @param top
     * @param left
     * @param right
     * @param bottom
     */
    protected final void setPadding( int top, int left, int right, int bottom )
    {
        paddingTop.setIntValue( top );
        paddingLeft.setIntValue( left );
        paddingRight.setIntValue( right );
        paddingBottom.setIntValue( bottom );
    }
    
    /**
     * Returns a {@link BorderWrapper}, that encapsulates the actual used border with convenience wrappers for the size getters.
     * The {@link BorderWrapper} instance is never null while the border can be null.
     * 
     * @return a {@link BorderWrapper} for the used Border (never null).
     */
    public final BorderWrapper getBorder()
    {
        return ( border.getBorder() );
    }
    
    /**
     * This method is called when a complete redraw has been forced.
     */
    protected void onCompleteRedrawForced()
    {
    }
    
    void forceCompleteRedraw_( boolean mergedBackgroundToo, boolean forwardCall )
    {
        boolean changed = !this.needsCompleteRedraw;
        
        this.needsCompleteRedraw = true;
        
        if ( ( background != null ) && mergedBackgroundToo )
            background.setMergedBGDirty();
        
        if ( forwardCall && ( masterWidget != null ) )
        if ( masterWidget != null )
            masterWidget.forceCompleteRedraw_( mergedBackgroundToo, false );
        
        setDirtyFlag();
        
        if ( changed )
            onCompleteRedrawForced();
    }
    
    /**
     * Forces a complete redraw on the next render.
     * 
     * @param mergedBackgroundToo if <code>true</code>, the clear-background will be redrawn and the {@link #drawBackground(LiveGameData, EditorPresets, TextureImage2D, int, int, int, int, boolean)} methods will be called again.
     */
    public final void forceCompleteRedraw( boolean mergedBackgroundToo )
    {
        forceCompleteRedraw_( mergedBackgroundToo, true );
    }
    
    /**
     * This simply calls {@link #forceCompleteRedraw(boolean)}, {@link #forceReinitialization()} and {@link #setDirtyFlag()}.
     * This method must be called after a value has been changed, that requires a reinitialization of all positioned strings, etc.
     */
    public final void forceAndSetDirty( boolean mergedBackgroundToo )
    {
        forceCompleteRedraw( mergedBackgroundToo );
        forceReinitialization();
        setDirtyFlag();
    }
    
    /**
     * Sets this Widget's visibility usually controlled by the ToggleWidgetVisibility InputAction.<br />
     * This flag is also restored when a different configurations is loaded unlike the others.
     * 
     * @param visible
     */
    public void setInputVisible( boolean visible )
    {
        boolean wasVisible = isVisible();
        
        this.inputVisible.setBooleanValue( visible );
        
        if ( isVisible() != wasVisible )
            onVisibilityChanged( visible );
    }
    
    /**
     * Gets this Widget's visibility usually controlled by the ToggleWidgetVisibility InputAction.<br />
     * This flag is also restored when a different configurations is loaded unlike the others.
     * 
     * @return this Widget's visibility flag.
     */
    public final boolean isInputVisible()
    {
        return ( inputVisible.getBooleanValue() );
    }
    
    /**
     * Sets this Widget's user visibility flag 1. This is the one, you should toggle in your widget code.
     * 
     * @param visible
     */
    public void setUserVisible1( boolean visible )
    {
        boolean wasVisible = isVisible();
        
        this.userVisible1 = visible;
        
        if ( isVisible() != wasVisible )
            onVisibilityChanged( visible );
    }
    
    /**
     * Gets this Widget's user visibility flag 1. This is the one, you should toggle in your widget code.
     * 
     * @return this Widget's visibility flag.
     */
    public final boolean isUserVisible1()
    {
        return ( userVisible1 );
    }
    
    /**
     * Sets this Widget's user visibility flag 2. This is the one, you should toggle in your widget code.
     * 
     * @param visible
     */
    public void setUserVisible2( boolean visible )
    {
        boolean wasVisible = isVisible();
        
        this.userVisible2 = visible;
        
        if ( isVisible() != wasVisible )
            onVisibilityChanged( visible );
    }
    
    /**
     * Gets this Widget's user visibility flag 2. This is the one, you should toggle in your widget code.
     * 
     * @return this Widget's visibility flag.
     */
    public final boolean isUserVisible2()
    {
        return ( userVisible2 );
    }
    
    /**
     * Gets this Widget's total visibility flag ({@link #isInputVisible()} && {@link #isUserVisible1()} && {@link #isUserVisible2()}).
     * 
     * @return this Widget's visibility flag.
     */
    public final boolean isVisible()
    {
        return ( inputVisible.getBooleanValue() && userVisible1 && userVisible2 );
    }
    
    public final boolean visibilityChangedSinceLastDraw()
    {
        return ( visibilityChangedSinceLastDraw );
    }
    
    /**
     * This method is called first by the rendering system each frame before {@link #isVisible()} is checked.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void updateVisibility( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    private final boolean needsCompleteRedraw()
    {
        boolean result = needsCompleteRedraw;
        
        needsCompleteRedraw = false;
        
        return ( result );
    }
    
    /**
     * Gets, whether this Widget has just been set invisible and its area hence needs to be cleared.
     * The flag is forced to false after this method has been called.
     * 
     * @return whether this Widget has just been set invisible and its area hence needs to be cleared.
     */
    final boolean needsCompleteClear()
    {
        boolean result = needsCompleteClear;
        
        needsCompleteClear = false;
        
        return ( result );
    }
    
    /**
     * Gets the data indicators for the data needed for this {@link Widget} to be drawn (bitmask).
     * 
     * @see #NEEDED_DATA_TELEMETRY
     * @see #NEEDED_DATA_SCORING
     * @see #NEEDED_DATA_SCORING
     * 
     * @return the data indicators for the data needed for this {@link Widget} to be drawn.
     */
    public int getNeededData()
    {
        return ( 0 );
    }
    
    /**
     * Gets the {@link Widget}'s {@link DrawnStringFactory}.
     * 
     * @return the {@link Widget}'s {@link DrawnStringFactory}.
     */
    protected final DrawnStringFactory getDrawnStringFactory()
    {
        return ( drawnStringFactory );
    }
    
    /**
     * This event is fired right after the {@link WidgetsConfiguration} has been (re-)loaded.
     * 
     * @param widgetsConfig
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void afterConfigurationLoaded( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This event is fired right before the {@link WidgetsConfiguration} is cleared.
     * 
     * @param widgetsConfig
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void beforeConfigurationCleared( WidgetsConfiguration widgetsConfig, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is executed when a new track was loaded.
     * 
     * @param trackname
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onTrackChanged( String trackname, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is executed when a new session was started.
     * 
     * @param sessionType
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onSessionStarted( SessionType sessionType, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the user entered realtime mode. If your {@link Widget} needs some data
     * to be drawn correctly, consider using {@link #onNeededDataComplete(LiveGameData, EditorPresets)}.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onRealtimeEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when {@link ScoringInfo} have been updated (done at 2Hz).
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onScoringInfoUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when {@link VehicleSetup} has been updated.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onVehicleSetupUpdated( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when the needed data is available in realtime mode.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onNeededDataComplete( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the car entered the pits.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onPitsEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the car entered the garage.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onGarageEntered( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the car exited the garage.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onGarageExited( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the car exited the pits.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onPitsExited( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a the user exited realtime mode.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onRealtimeExited( LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when either the player's vehicle control has changed or another vehicle is being viewed.
     * 
     * @param viewedVSI
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onVehicleControlChanged( VehicleScoringInfo viewedVSI, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This method is called when a lap has been finished and a new one was started.
     * 
     * @param vsi the driver, who started the lap. If this is the leader and the session type is RACE, the whole race has moved on to the next lap.
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     */
    public void onLapStarted( VehicleScoringInfo vsi, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * This event is fired, when a bound input component has changed its state.
     * 
     * @param action
     * @param state
     * @param modifierMask see {@link InputAction}
     * @param when
     * @param gameData
     * @param editorPresets
     */
    public void onBoundInputStateChanged( InputAction action, boolean state, int modifierMask, long when, LiveGameData gameData, EditorPresets editorPresets )
    {
    }
    
    /**
     * Returns <code>true</code>, if this {@link Widget} draws on the main texture, <code>false</code> otherwise.<br />
     * Default is <code>true</code>.
     * 
     * @param isEditorMode
     * 
     * @return <code>true</code>, if this {@link Widget} draws on the main texture, <code>false</code> otherwise.
     */
    public boolean hasMasterCanvas( boolean isEditorMode )
    {
        return ( true );
    }
    
    /**
     * 
     * @param isEditorMode true, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     */
    public void clearRegion( boolean isEditorMode, TextureImage2D texture )
    {
        if ( ( texture == null ) && !isEditorMode )
            return;
        
        int offsetX = position.getEffectiveX();
        int offsetY = position.getEffectiveY();
        int width = size.getEffectiveWidth();
        int height = size.getEffectiveHeight();
        
        texture.getTextureCanvas().pushClip( offsetX, offsetY, width, height );
        
        texture.clear( offsetX, offsetY, width, height, true, null );
        
        texture.getTextureCanvas().popClip();
        
        this.visibilityChangedSinceLastDraw = false;
    }
    
    /**
     * Clears the given part of the {@link Widget} area with the current background.
     * 
     * @param texture the target texture
     * @param offsetX the x offset of the {@link Widget} on the drawing texture
     * @param offsetY the y offset of the {@link Widget} on the drawing texture
     * @param localX the x coordinate of the upper left corner of the area to be cleared relative to the Widget's location
     * @param localY the y coordinate of the upper left corner of the area to be cleared relative to the Widget's location
     * @param width the width of the area to be cleared
     * @param height the height of the area to be cleared
     * @param markDirty if true, the pixel is marked dirty
     * @param dirtyRect if non null, the dirty rect is written to this instance
     * 
     * @return <code>true</code>, if this Widgets defines a background to clear with, <code>false</code> otherwise.
     */
    public boolean clearBackgroundRegion( TextureImage2D texture, int offsetX, int offsetY, int localX, int localY, int width, int height, boolean markDirty, Rect2i dirtyRect )
    {
        if ( getMasterWidget() != null )
        {
            int effX = getPosition().getEffectiveX();
            int effY = getPosition().getEffectiveY();
            
            return ( getMasterWidget().clearBackgroundRegion( texture, offsetX - effX, offsetY - effY, localX + effX, localY + effY, width, height, markDirty, dirtyRect ) );
        }
        
        final WidgetBackground background = getBackground();
        
        if ( background == null )
        {
            if ( dirtyRect != null )
                dirtyRect.set( -1, -1, 0, 0 );
            
            return ( false );
        }
        
        TextureImage2D mergedBG = background.getMergedTexture();
        if ( mergedBG != null )
        {
            texture.clear( mergedBG, localX, localY, width, height, offsetX + localX, offsetY + localY, markDirty, dirtyRect );
            
            return ( true );
        }
        
        if ( background.getType().isColor() )
        {
            texture.clear( background.getColor(), offsetX + localX, offsetY + localY, width, height, markDirty, dirtyRect );
            
            return ( true );
        }
        
        if ( background.getType().isImage() )
        {
            texture.clear( background.getTexture(), localX, localY, width, height, offsetX + localX, offsetY + localY, markDirty, dirtyRect );
            
            return ( true );
        }
        
        return ( false );
    }
    
    /**
     * This method is called once to initialized {@link DrawnString}s used on this Widget.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param gameData the live game data
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param drawnStringFactory
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param width the width on the texture
     * @param height the height on the texture
     */
    protected abstract void initialize( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, DrawnStringFactory drawnStringFactory, TextureImage2D texture, int offsetX, int offsetY, int width, int height );
    
    /**
     * Checks, if the Widget needs any changes before it is drawn. If true, {@link #drawBorder(boolean, BorderWrapper, TextureImage2D, int, int, int, int)}
     * and possibly {@link #drawBackground(LiveGameData, EditorPresets, TextureImage2D, int, int, int, int, boolean)} are (re-)invoked.<br />
     * The original method is just an empty stub returning false.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param gameData the live game data
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param width the width on the texture
     * @param height the height on the texture
     * 
     * @return true, if size has changed.
     */
    protected boolean checkForChanges( boolean clock1, boolean clock2, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        return ( false );
    }
    
    /**
     * 
     * @param isEditorMode
     * @param border
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     */
    protected void drawBorder( boolean isEditorMode, BorderWrapper border, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( hasBorder() && ( texture != null ) )
        {
            border.drawBorder( ( ( background == null ) || !background.getType().isColor() ) ? null : background.getColor(), texture, offsetX, offsetY, width, height );
        }
    }
    
    /**
     * You can use this method to directly draw static content onto your Widget's background.
     * Overriding this method makes the Widget use a background texture no matter, if the background is defined with a color only or an image.
     * 
     * @param gameData
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     * @param isRoot if this is true, you can possibly clear your stuff onto the texture instead of drawing it.
     */
    protected void drawBackground( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        if ( canHaveBackground() )
        {
            if ( background.getType().isColor() )
            {
                if ( isRoot )
                    texture.clear( background.getColor(), offsetX, offsetY, width, height, false, null );
                else if ( background.getColor().getAlpha() > 0 )
                    texture.fillRectangle( background.getColor(), offsetX, offsetY, width, height, false, null );
            }
            else if ( background.getType().isImage() )
            {
                if ( isRoot )
                    texture.clear( background.getTexture(), 0, 0, width, height, offsetX, offsetY, width, height, false, null );
                else
                    texture.drawImage( background.getTexture(), 0, 0, width, height, offsetX, offsetY, width, height, false, null );
            }
        }
    }
    
    void drawBackground_( LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height, boolean isRoot )
    {
        texture.getTextureCanvas().pushClip( offsetX, offsetY, width, height, true );
        
        try
        {
            drawBackground( gameData, editorPresets, texture, offsetX, offsetY, width, height, isRoot );
        }
        finally
        {
            texture.getTextureCanvas().popClip();
        }
    }
    
    /**
     * @param background never <code>null</code>!
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX
     * @param offsetY
     * @param width
     * @param height
     */
    private final void clearBackground( WidgetBackground background, TextureImage2D texture, int offsetX, int offsetY, int width, int height )
    {
        if ( texture != null )
        {
            TextureImage2D mergedBG = background.getMergedTexture();
            
            if ( mergedBG == null )
            {
                if ( background.getColor() != null )
                    texture.clear( background.getColor(), offsetX, offsetY, width, height, true, null );
                else if ( background.getTexture() != null )
                    texture.clear( background.getTexture(), offsetX, offsetY, width, height, true, null );
                else
                    texture.clear( offsetX, offsetY, width, height, true, null );
            }
            else
            {
                texture.clear( mergedBG, offsetX, offsetY, width, height, true, null );
            }
        }
    }
    
    /**
     * This method must contain the actual drawing code for this Widget.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param needsCompleteRedraw whether this widget needs to be completely redrawn (true) or just the changed parts (false)
     * @param gameData the live game data
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     * @param offsetX the x-offset on the texture
     * @param offsetY the y-offset on the texture
     * @param width the width on the texture
     * @param height the height on the texture
     */
    protected abstract void drawWidget( boolean clock1, boolean clock2, boolean needsCompleteRedraw, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture, int offsetX, int offsetY, int width, int height );
    
    /**
     * This method invokes the parts of the actual drawing code for this Widget.
     * 
     * @param clock1 this is a small-stepped clock for very dynamic content, that needs smooth display. If 'needsCompleteRedraw' is true, clock1 is also true.
     * @param clock2 this is a larger-stepped clock for very dynamic content, that doesn't need smooth display. If 'needsCompleteRedraw' is true, clock2 is also true.
     * @param completeRedrawForced
     * @param gameData the live game data
     * @param editorPresets non null, if the Editor is used for rendering instead of rFactor
     * @param texture the texture image to draw on. Use {@link TextureImage2D#getTextureCanvas()} to retrieve the {@link Texture2DCanvas} for Graphics2D drawing.
     */
    public final void drawWidget( boolean clock1, boolean clock2, boolean completeRedrawForced, LiveGameData gameData, EditorPresets editorPresets, TextureImage2D texture )
    {
        boolean wasInitialized = initialized;
        
        int offsetX = position.getEffectiveX();
        int offsetY = position.getEffectiveY();
        int width = size.getEffectiveWidth();
        int height = size.getEffectiveHeight();
        
        int borderOLW = getBorder().getOpaqueLeftWidth();
        int borderOTH = getBorder().getOpaqueTopHeight();
        int borderORW = getBorder().getOpaqueRightWidth();
        int borderOBH = getBorder().getOpaqueBottomHeight();
        
        int borderLW = getBorder().getInnerLeftWidth();
        int borderTH = getBorder().getInnerTopHeight();
        int borderRW = getBorder().getInnerRightWidth();
        int borderBH = getBorder().getInnerBottomHeight();
        
        int offsetX2 = offsetX + borderLW;
        int offsetY2 = offsetY + borderTH;
        int width2 = width - borderLW - borderRW;
        int height2 = height - borderTH - borderBH;
        
        final Texture2DCanvas texCanvas = texture.getTextureCanvas();
        final TextureImage2D texture2 = hasMasterCanvas( editorPresets != null ) ? texture : null;
        
        if ( !initialized )
        {
            initialize( clock1, clock2, gameData, editorPresets, drawnStringFactory, texture2, offsetX2, offsetY2, width2, height2 );
            
            initialized = true;
        }
        
        if ( checkForChanges( clock1, clock2, gameData, editorPresets, texture2, offsetX2, offsetY2, width2, height2 ) )
        {
            forceCompleteRedraw( true );
            completeRedrawForced = true;
            
            offsetX = position.getEffectiveX();
            offsetY = position.getEffectiveY();
            width = size.getEffectiveWidth();
            height = size.getEffectiveHeight();
            
            offsetX2 = offsetX + borderLW;
            offsetY2 = offsetY + borderTH;
            width2 = width - borderLW - borderRW;
            height2 = height - borderTH - borderBH;
        }
        
        texCanvas.setClip( offsetX, offsetY, width, height );
        
        completeRedrawForced = needsCompleteRedraw() || completeRedrawForced;
        
        if ( completeRedrawForced )
        {
            __RenderPrivilegedAccess.onWidgetCleared( drawnStringFactory );
            
            drawBorder( ( editorPresets != null ), getBorder(), texture2, offsetX, offsetY, width, height );
            
            if ( texture2 != null )
                texture2.markDirty( offsetX, offsetY, width, height );
            
            if ( ( getMasterWidget() == null ) && ( background != null ) )
            {
                background.updateMergedBackground( gameData, editorPresets );
                
                clearBackground( background, ( editorPresets == null ) ? texture2 : texture, offsetX2 - getBorder().getPaddingLeft(), offsetY2 - getBorder().getPaddingTop(), width2 + getBorder().getPaddingLeft() + getBorder().getPaddingRight(), height2 + getBorder().getPaddingTop() + getBorder().getPaddingBottom() );
            }
        }
        
        texCanvas.setClip( offsetX + borderOLW, offsetY + borderOTH, width - borderOLW - borderORW, height - borderOTH - borderOBH );
        
        drawWidget( clock1, clock2, completeRedrawForced, gameData, editorPresets, texture2, offsetX2, offsetY2, width2, height2 );
        
        if ( editorPresets != null )
        {
            initialized = wasInitialized;
            TransformableTexture[] subTextures = getSubTextures( gameData, editorPresets, width2, height2 );
            initialized = true;
            
            if ( subTextures != null )
            {
                texCanvas.setClip( (Rect2i)null );
                
                for ( int i = 0; i < subTextures.length; i++ )
                {
                    subTextures[i].drawInEditor( texCanvas, offsetX2 + subTextures[i].getOwnerWidget().getOffsetXToMasterWidget(), offsetY2 + subTextures[i].getOwnerWidget().getOffsetYToMasterWidget() );
                }
            }
        }
        
        this.visibilityChangedSinceLastDraw = false;
    }
    
    
    /**
     * Saves all settings to the config file.
     * 
     * @param writer
     */
    public void saveProperties( WidgetsConfigurationWriter writer ) throws IOException
    {
        writer.writeProperty( position.getPositioningProperty( "positioning" ), "The way, position coordinates are interpreted (relative to). Valid values: TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER_CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT." );
        writer.writeProperty( position.getXProperty( "x" ), "The x-coordinate for the position." );
        writer.writeProperty( position.getYProperty( "y" ), "The y-coordinate for the position." );
        writer.writeProperty( size.getWidthProperty( "width" ), "The width. Use negative values to make the Widget be sized relative to screen size." );
        writer.writeProperty( size.getHeightProperty( "height" ), "The height. Use negative values to make the Widget be sized relative to screen size." );
        writer.writeProperty( border, "The widget's border." );
        if ( masterWidget == null )
        {
            writer.writeProperty( paddingTop, "top padding" );
            writer.writeProperty( paddingLeft, "left padding" );
            writer.writeProperty( paddingRight, "right padding" );
            writer.writeProperty( paddingBottom, "bottom padding" );
            writer.writeProperty( inputVisible, "The initial visibility." );
        }
        
        if ( canHaveBackground() )
        {
            writer.writeProperty( backgroundProperty, "The Widget's background (color or image)." );
        }
        
        if ( hasText() )
        {
            writer.writeProperty( font, "The used font." );
            writer.writeProperty( fontColor, "The Widget's font color in the format #RRGGBB (hex)." );
        }
    }
    
    /**
     * Loads (and parses) a certain property from a config file.
     * 
     * @param loader
     */
    public void loadProperty( PropertyLoader loader )
    {
        if ( loader.loadProperty( name ) );
        else if ( loader.loadProperty( position.getPositioningProperty( "positioning" ) ) );
        else if ( loader.loadProperty( position.getXProperty( "x" ) ) );
        else if ( loader.loadProperty( position.getYProperty( "y" ) ) );
        else if ( loader.loadProperty( size.getWidthProperty( "width" ) ) );
        else if ( loader.loadProperty( size.getHeightProperty( "height" ) ) );
        else if ( loader.loadProperty( border ) );
        else if ( loader.loadProperty( paddingTop ) );
        else if ( loader.loadProperty( paddingLeft ) );
        else if ( loader.loadProperty( paddingRight ) );
        else if ( loader.loadProperty( paddingBottom ) );
        else if ( loader.loadProperty( inputVisible ) );
        else if ( canHaveBackground() && loader.loadProperty( backgroundProperty ) );
        else if ( loader.loadProperty( font ) );
        else if ( loader.loadProperty( fontColor ) );
    }
    
    /**
     * Adds the border property to the container.
     * 
     * @param property
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void addBorderPropertyToContainer( BorderProperty property, WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( property );
    }
    
    /**
     * Adds the padding properties to the container.
     * 
     * @param paddingTop
     * @param paddingLeft
     * @param paddingRight
     * @param paddingBottom
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void addPaddingPropertiesToContainer( IntProperty paddingTop, IntProperty paddingLeft, IntProperty paddingRight, IntProperty paddingBottom, WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.pushGroup( "Padding", false );
        
        propsCont.addProperty( paddingTop );
        propsCont.addProperty( paddingLeft );
        propsCont.addProperty( paddingRight );
        propsCont.addProperty( paddingBottom );
        
        propsCont.popGroup();
    }
    
    
    /**
     * Adds the background property to the container.
     * 
     * @param property
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void addBackgroundPropertyToContainer( BackgroundProperty property, WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( property );
    }
    
    
    /**
     * Adds the font and font color properties to the container.
     * 
     * @param font
     * @param fontColor
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void addFontPropertiesToContainer( FontProperty font, ColorProperty fontColor, WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addProperty( font );
        propsCont.addProperty( fontColor );
    }
    
    /**
     * Puts all editable properties to the editor.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    protected void getPropertiesForParentGroup( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
    }
    
    /**
     * Puts all editable properties to the editor.
     * 
     * @param propsCont the container to add the properties to
     * @param forceAll If <code>true</code>, all properties provided by this {@link Widget} must be added.
     *                 If <code>false</code>, only the properties, that are relevant for the current {@link Widget}'s situation have to be added, some can be ignored.
     */
    public void getProperties( WidgetPropertiesContainer propsCont, boolean forceAll )
    {
        propsCont.addGroup( "General" );
        
        propsCont.addProperty( type );
        propsCont.addProperty( name );
        
        if ( masterWidget == null )
        {
            propsCont.addProperty( inputVisible );
        }
        propsCont.addProperty( position.getPositioningProperty( "positioning" ) );
        propsCont.addProperty( position.getXProperty( "x" ) );
        propsCont.addProperty( position.getYProperty( "y" ) );
        propsCont.addProperty( size.getWidthProperty( "width" ) );
        propsCont.addProperty( size.getHeightProperty( "height" ) );
        
        if ( masterWidget == null )
        {
            addPaddingPropertiesToContainer( paddingTop, paddingLeft, paddingRight, paddingBottom, propsCont, forceAll );
        }
        
        if ( ( masterWidget == null ) && hasBorder() )
        {
            addBorderPropertyToContainer( border, propsCont, forceAll );
        }
        
        if ( canHaveBackground() )
        {
            addBackgroundPropertyToContainer( backgroundProperty, propsCont, forceAll );
        }
        
        if ( hasText() )
        {
            addFontPropertiesToContainer( font, fontColor, propsCont, forceAll );
        }
        
        getPropertiesForParentGroup( propsCont, forceAll );
        
        //propsCont.dump();
    }
    
    private String getDocumentationSource( Class<?> clazz, Property property )
    {
        URL docURL = null;
        
        if ( property == null )
            docURL = this.getClass().getClassLoader().getResource( clazz.getPackage().getName().replace( '.', '/' ) + "/doc/widget.html" );
        else
            docURL = this.getClass().getClassLoader().getResource( clazz.getPackage().getName().replace( '.', '/' ) + "/doc/" + property.getName() + ".html" );
        
        if ( docURL == null )
        {
            if ( ( clazz.getSuperclass() != null ) && ( clazz.getSuperclass() != Object.class ) )
                return ( getDocumentationSource( clazz.getSuperclass(), property ) );
            
            return ( "" );
        }
        
        return ( StringUtil.loadString( docURL ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String getDocumentationSource( Property property )
    {
        return ( getDocumentationSource( this.getClass(), property ) );
    }
    
    
    
    /**
     * Defines, if this Widget type can have a border.
     * 
     * @return if this Widget type can have a border.
     */
    protected boolean canHaveBorder()
    {
        return ( true );
    }
    
    /**
     * Defines, if this Widget type can have a background.
     * 
     * @return if this Widget type can have a background.
     */
    protected boolean canHaveBackground()
    {
        return ( true );
    }
    
    /**
     * Gets whether this {@link Widget} has a border or not.
     * 
     * @return whether this {@link Widget} has a border or not.
     */
    protected final boolean hasBorder()
    {
        if ( !canHaveBorder() )
            return ( false );
        
        if ( !getBorder().hasBorder() )
            return ( false );
        
        /*
        if ( background == null )
            return ( false );
        
        return ( background.getType().isColor() );
        */
        return ( true );
    }
    
    /**
     * Defines, if a Widget type (potentially) contains any text.
     * If <code>false</code>, the editor won't provide font or font-color selection.
     * Should return a contant value.
     * 
     * @return if this Widget can contain any text.
     */
    protected boolean hasText()
    {
        return ( true );
    }
    
    /**
     * Creates a new {@link Widget}.
     * 
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param widthPercent width parameter treated as percents
     * @param height negative numbers for (screen_height - height)
     * @param heightPercent height parameter treated as percents
     */
    protected Widget( String name, float width, boolean widthPercent, float height, boolean heightPercent )
    {
        this.name.setStringValue( name );
        this.size = Size.newGlobalSize( this, width, widthPercent, height, heightPercent );
        this.innerSize = new InnerSize( size, border );
        this.position = Position.newGlobalPosition( this, RelativePositioning.TOP_LEFT, 0f, true, 0f, true, size );
        
        if ( !canHaveBorder() )
            border.setBorder( null );
    }
    
    /**
     * Creates a new {@link Widget}.
     * 
     * @param name
     * @param width negative numbers for (screen_width - width)
     * @param height negative numbers for (screen_height - height)
     */
    protected Widget( String name, float width, float height )
    {
        this( name, width, true, height, true );
    }
}

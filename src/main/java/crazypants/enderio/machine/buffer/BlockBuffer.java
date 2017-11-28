package crazypants.enderio.machine.buffer;

import javax.annotation.Nonnull;

import crazypants.enderio.GuiID;
import crazypants.enderio.init.IModObject;
import crazypants.enderio.machine.MachineObject;
import crazypants.enderio.machine.base.block.AbstractMachineBlock;
import crazypants.enderio.machine.render.RenderMappers;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.paint.IPaintable;
import crazypants.enderio.render.IBlockStateWrapper;
import crazypants.enderio.render.IRenderMapper;
import crazypants.enderio.render.IRenderMapper.IItemRenderMapper;
import crazypants.enderio.render.property.EnumRenderMode;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBuffer extends AbstractMachineBlock<TileBuffer> implements IPaintable.ISolidBlockPaintableBlock, IPaintable.IWrenchHideablePaint {

  public static BlockBuffer create(@Nonnull IModObject modObject) {
    PacketHandler.INSTANCE.registerMessage(PacketBufferIO.class, PacketBufferIO.class, PacketHandler.nextID(), Side.SERVER);
    BlockBuffer res = new BlockBuffer();
    res.init();
    return res;
  }

  private BlockBuffer() {
    super(MachineObject.blockBuffer, TileBuffer.class);
    setDefaultState(this.blockState.getBaseState().withProperty(EnumRenderMode.RENDER, EnumRenderMode.AUTO).withProperty(BufferType.TYPE, BufferType.ITEM));
  }
  
	@Override
	public Item createBlockItem(IModObject modObject) {
    return modObject.apply(new BlockItemBuffer((Block) this));
	}

  @Override
  protected @Nonnull BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, new IProperty[] { EnumRenderMode.RENDER, BufferType.TYPE });
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty(BufferType.TYPE, BufferType.getTypeFromMeta(meta));
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return BufferType.getMetaFromType(state.getValue(BufferType.TYPE));
  }

  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    return state.withProperty(EnumRenderMode.RENDER, EnumRenderMode.AUTO);
  }

  @Override
  public int damageDropped(IBlockState st) {
    return getMetaFromState(st);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(Item item, CreativeTabs tab, NonNullList<ItemStack> list) {
    for (BufferType type : BufferType.values()) {
      list.add(new ItemStack(item, 1, type.ordinal()));
    }
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if (te instanceof TileBuffer) {
      return new ContainerBuffer(player.inventory, (TileBuffer) te);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if (te instanceof TileBuffer) {
      return new GuiBuffer(player.inventory, (TileBuffer) te);
    }
    return null;
  }

  @Override
  protected GuiID getGuiId() {
    return GuiID.GUI_ID_BUFFER;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IItemRenderMapper getItemRenderMapper() {
    return RenderMappers.FRONT_MAPPER;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IRenderMapper.IBlockRenderMapper getBlockRenderMapper() {
    return RenderMappers.FRONT_MAPPER;
  }

  @Override
  protected void setBlockStateWrapperCache(@Nonnull IBlockStateWrapper blockStateWrapper, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
      @Nonnull TileBuffer tileEntity) {
    blockStateWrapper.addCacheKey(blockStateWrapper.getValue(BufferType.TYPE));
  }

  @Override
  public boolean hasComparatorInputOverride(IBlockState state) {
    return state.getValue(BufferType.TYPE).hasInventory;
  }

  @Override
  public int getComparatorInputOverride(IBlockState blockState1, World worldIn, BlockPos pos) {
    return Container.calcRedstone(getTileEntity(worldIn, pos));
  }

}